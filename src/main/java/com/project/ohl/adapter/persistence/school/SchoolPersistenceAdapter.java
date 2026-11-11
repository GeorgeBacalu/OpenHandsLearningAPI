package com.project.ohl.adapter.persistence.school;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.directory.CityEntity;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.domain.model.School;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SchoolPersistenceAdapter extends JpaPersistenceAdapter<School, SchoolEntity> implements SchoolRepositoryPort {

    public SchoolPersistenceAdapter(SchoolJpaRepository repository) {
        super(repository);
    }

    @Override
    protected SchoolEntity newEntity() {
        return new SchoolEntity();
    }

    @Override
    protected UUID idOf(School model) {
        return model.getId();
    }

    @Override
    protected School toDomain(SchoolEntity entity) {
        return School.builder().id(entity.getId())
              .name(entity.getName()).cityId(entity.getCity() == null ? null : entity.getCity().getId())
              .build();
    }

    @Override
    protected void apply(School model, SchoolEntity entity) {
        entity.setName(model.getName());
        entity.setCity(reference(CityEntity.class, model.getCityId()));
    }
}
