package com.project.ohl;

import com.project.ohl.adapter.persistence.category.CategoryEntity;
import com.project.ohl.adapter.persistence.category.CategoryJpaRepository;
import com.project.ohl.adapter.persistence.directory.CityJpaRepository;
import com.project.ohl.adapter.persistence.directory.CountyJpaRepository;
import com.project.ohl.adapter.persistence.favorite.FavoriteJpaRepository;
import com.project.ohl.adapter.persistence.school.SchoolJpaRepository;
import com.project.ohl.adapter.persistence.user.UserEntity;
import com.project.ohl.adapter.persistence.user.UserJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Sql(scripts = {"/cleanup.sql", "/test.sql"})
public abstract class DatabaseIntegrationTest {
    @Autowired protected UserJpaRepository userRepository;
    @Autowired protected CategoryJpaRepository categoryRepository;
    @Autowired protected SchoolJpaRepository schoolRepository;
    @Autowired protected CountyJpaRepository countyRepository;
    @Autowired protected CityJpaRepository cityRepository;
    @Autowired protected FavoriteJpaRepository favoriteRepository;
    @Autowired private PlatformTransactionManager transactions;
    @PersistenceContext protected EntityManager entityManager;

    @BeforeEach
    final void assertSeededDatabase() {
        assertRowCounts(2);
    }

    @AfterEach
    final void clearDatabase() {
        inTransaction(() -> {
            favoriteRepository.deleteAllInBatch();
            entityManager.createQuery("update CategoryEntity c set c.parent = null").executeUpdate();
            categoryRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();
            schoolRepository.deleteAllInBatch();
            cityRepository.deleteAllInBatch();
            countyRepository.deleteAllInBatch();
            entityManager.clear();
        });
        assertRowCounts(0);
    }

    private void assertRowCounts(long expected) {
        assertThat(favoriteRepository.count()).as("Favorites").isEqualTo(expected);
        assertThat(schoolRepository.count()).as("Schools").isEqualTo(expected);
        assertThat(categoryRepository.count()).as("Categories").isEqualTo(expected);
        assertThat(userRepository.count()).as("Users").isEqualTo(expected);
        assertThat(countyRepository.count()).as("Counties").isEqualTo(expected);
        assertThat(cityRepository.count()).as("Cities").isEqualTo(expected);
    }

    protected final void inTransaction(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> action.run());
    }

    protected final void updateUser(UUID id, Consumer<UserEntity> change) {
        inTransaction(() -> {
            change.accept(userRepository.findById(id).orElseThrow());
            userRepository.flush();
        });
    }

    protected final void setFixturePassword(String hash) {
        inTransaction(() -> {
            userRepository.findAll().forEach(user -> user.setPasswordHash(hash));
            userRepository.flush();
        });
    }

    protected final void updateCategory(UUID id, Consumer<CategoryEntity> change) {
        inTransaction(() -> {
            change.accept(categoryRepository.findById(id).orElseThrow());
            categoryRepository.flush();
        });
    }

    protected final UUID createCategory(String name, String imageUrl) {
        var entity = new CategoryEntity();
        entity.setName(name);
        entity.setImageUrl(imageUrl);
        return categoryRepository.saveAndFlush(entity).getId();
    }

    // Schema metadata and deliberately invalid SQL cannot be expressed as entity operations.
    protected final <T> List<T> nativeValues(String sql, Class<T> type) {
        List<?> values = entityManager.createNativeQuery(sql, type).getResultList();
        return values.stream().map(type::cast).toList();
    }

    protected final long schemaCount(String sql) {
        return nativeValues(sql, Long.class).getFirst();
    }

    protected final void executeNative(String sql, Object... parameters) {
        inTransaction(() -> {
            var query = entityManager.createNativeQuery(sql);
            for (int index = 0; index < parameters.length; index++) query.setParameter(index + 1, parameters[index]);
            query.executeUpdate();
        });
    }
}
