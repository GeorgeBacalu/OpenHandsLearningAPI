package com.project.ohl.adapter.persistence.directory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL transaction locking and legacy schema validation have no JPA equivalent. */
@Transactional
public class DirectoryImportOperationsImpl implements DirectoryImportOperations {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void lockImport() {
        entityManager.createNativeQuery("SELECT 1 FROM pg_advisory_xact_lock(782134920)", Integer.class).getSingleResult();
    }

    @Override
    public void validateSchoolCities() {
        entityManager.flush();
        entityManager.createNativeQuery("ALTER TABLE schools VALIDATE CONSTRAINT schools_city_required").executeUpdate();
    }
}
