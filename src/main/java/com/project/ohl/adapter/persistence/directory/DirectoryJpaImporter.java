package com.project.ohl.adapter.persistence.directory;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.school.SchoolEntity;
import com.project.ohl.adapter.persistence.school.SchoolJpaRepository;
import com.project.ohl.domain.model.SchoolDirectory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DirectoryJpaImporter {
    private static final int BATCH_SIZE = 500;
    private final CountyJpaRepository counties;
    private final CityJpaRepository cities;
    private final SchoolJpaRepository schools;
    @PersistenceContext private EntityManager entityManager;

    @Transactional
    public void importDirectory(SchoolDirectory directory) {
        var session = entityManager.unwrap(Session.class);
        Integer previousBatchSize = session.getJdbcBatchSize();
        session.setJdbcBatchSize(BATCH_SIZE);
        try {
            upsert(directory.counties(), counties, SchoolDirectory.County::id, CountyEntity::new,
                    (row, entity) -> entity.setName(row.name()));
            upsert(directory.cities(), cities, SchoolDirectory.City::id, CityEntity::new, (row, entity) -> {
                entity.setName(row.name());
                entity.setCounty(counties.getReferenceById(row.countyId()));
            });
            upsert(directory.schools(), schools, SchoolDirectory.School::id, SchoolEntity::new, (row, entity) -> {
                entity.setName(row.name());
                entity.setCity(cities.getReferenceById(row.cityId()));
            });
        } finally {
            session.setJdbcBatchSize(previousBatchSize);
        }
    }

    private <S, E extends PersistentEntity> void upsert(List<S> rows, JpaRepository<E, UUID> repository,
            Function<S, UUID> id, Supplier<E> newEntity, BiConsumer<S, E> apply) {
        for (int start = 0; start < rows.size(); start += BATCH_SIZE) {
            var batch = rows.subList(start, Math.min(start + BATCH_SIZE, rows.size()));
            var existing = repository.findAllById(batch.stream().map(id).toList()).stream()
                    .collect(Collectors.toMap(PersistentEntity::getId, Function.identity()));
            var entities = batch.stream().map(row -> {
                E entity = existing.computeIfAbsent(id.apply(row), key -> {
                    E created = newEntity.get();
                    created.setId(key);
                    return created;
                });
                apply.accept(row, entity);
                return entity;
            }).toList();
            repository.saveAllAndFlush(entities);
            // Each batch is flushed before clearing so imports do not retain the entire directory.
            entityManager.clear();
        }
    }
}
