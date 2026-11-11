package com.project.ohl.adapter.persistence;

import java.util.UUID;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class JpaPersistenceAdapter<M, E extends PersistentEntity> {
    protected final JpaRepository<E, UUID> repository;
    @PersistenceContext
    private EntityManager entityManager;

    protected JpaPersistenceAdapter(JpaRepository<E, UUID> repository) {
        this.repository = repository;
    }

    protected <T extends PersistentEntity> T reference(Class<T> type, UUID id) {
        return id == null ? null : entityManager.getReference(type, id);
    }

    protected abstract E newEntity();
    protected abstract M toDomain(E entity);
    protected abstract void apply(M model, E entity);
    protected abstract UUID idOf(M model);

    protected Sort defaultSort() {
        return Sort.by("id");
    }

    @Transactional(readOnly = true)
    public List<M> findAll() {
        return repository.findAll(defaultSort()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<M> findById(UUID id) {
        return id == null ? Optional.empty() : repository.findById(id).map(this::toDomain);
    }

    @Transactional
    public M save(M model) {
        E entity = newEntity();
        apply(model, entity);
        return toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public M update(M model) {
        E entity = repository.findById(idOf(model))
              .orElseThrow(() -> new ResourceNotFoundException("Resource " + idOf(model) + " was not found"));
        apply(model, entity);
        return toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(M model) {
        repository.deleteById(idOf(model));
    }
}
