package com.project.ohl.adapter.persistence;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class JpaPersistenceAdapter<M, E extends PersistentEntity> {
    protected final JpaRepository<E, Long> repository;

    protected JpaPersistenceAdapter(JpaRepository<E, Long> repository) {
        this.repository = repository;
    }

    protected abstract E newEntity();
    protected abstract M toDomain(E entity);
    protected abstract void apply(M model, E entity);
    protected abstract Long idOf(M model);

    @Transactional(readOnly = true)
    public List<M> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<M> findById(Long id) {
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
