package com.project.ohl.persistence;

import com.project.ohl.DatabaseIntegrationTest;
import com.project.ohl.TestIds;
import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.School;
import com.project.ohl.domain.model.User;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SCHOOL_A;
import static com.project.ohl.TestIds.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PersistenceIntegrationTest extends DatabaseIntegrationTest {
    @Autowired private SchoolRepositoryPort schools;
    @Autowired private UserRepositoryPort users;
    @Autowired private CategoryRepositoryPort categories;
    @Autowired private CategoryService categoryService;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private UserService userService;
    @Autowired private PasswordHasher passwords;

    @Test
    void rejectsSelfParentsAndDescendantParents() {
        var parent = categories.findById(CATEGORY_PARENT).orElseThrow();
        var child = categories.findById(CATEGORY_CHILD).orElseThrow();
        var update = categoryMapper.convertToDto(parent);

        update.setParentId(parent.getId());
        assertThatThrownBy(() -> categoryService.updateCategoryById(update, parent.getId()))
                .isInstanceOf(InvalidRequestException.class);

        update.setParentId(child.getId());
        assertThatThrownBy(() -> categoryService.updateCategoryById(update, parent.getId()))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void hashesPasswordsBeforeSavingUsers() {
        var school = schools.findById(SCHOOL_A).orElseThrow();
        var role = Role.USER;
        var request = UserDto.builder().name("Ana")
                .email("hashed@example.test").password("long-test-password").phone("0712345678")
                .schoolId(school.getId()).role(role).build();
        var created = userService.saveUser(request);
        var stored = users.findById(created.getId()).orElseThrow().getPasswordHash();

        assertThat(stored).startsWith("$2").doesNotContain(request.getPassword());
        assertThat(passwords.matches(request.getPassword(), stored)).isTrue();

        request.setPassword("another-test-password");

        userService.updateUserById(request, created.getId());

        var updated = users.findById(created.getId()).orElseThrow().getPasswordHash();

        assertThat(updated).isNotEqualTo(stored);
        assertThat(passwords.matches(request.getPassword(), updated)).isTrue();
    }

    @Test
    void persistsAndUpdatesEveryResourceUsingTheHibernateSchema() {
        var school = schools.save(School.builder().name("School").cityId(UUID.fromString("00000000-0000-4000-8000-000000000104")).build());
        var role = Role.USER;
        var user = users.save(User.builder().name("Ana").email("created@example.test")
              .passwordHash("$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy").phone("0712345678")
              .school(school).role(role).build());
        var parent = categories.findById(CATEGORY_PARENT).orElseThrow();
        var child = categories.save(Category.builder().imageUrl("https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/test_image.webp").name("C").parent(parent).build());

        assertThat(List.of(school.getId(), user.getId(),
                child.getId()))
                .allSatisfy(value -> assertThat(value.version()).isEqualTo(4));

        assertThat(users.findById(user.getId()).orElseThrow().getSchool().getId()).isEqualTo(school.getId());
        assertThat(users.findById(user.getId()).orElseThrow().getRole()).isEqualTo(role);
        assertThat(categories.findById(child.getId()).orElseThrow().getParent().getId()).isEqualTo(parent.getId());

        school.setName("Updated School");
        user.setRole(Role.ADMIN);
        user.setName("Updated Ana");
        child.setName("Updated category");

        assertThat(schools.update(school).getName()).isEqualTo("Updated School");
        assertThat(users.update(user)).satisfies(updated -> {
            assertThat(updated.getName()).isEqualTo("Updated Ana");
            assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        });
        assertThat(categories.update(child).getName()).isEqualTo("Updated category");

        categories.delete(child);
        users.delete(user);
        schools.delete(school);
        assertThat(categories.findById(child.getId())).isEmpty();
        assertThat(users.findById(user.getId())).isEmpty();
    }

    @Test
    void categoryListsUseOneQueryAndReturnTheActualParentId() {
        var parent = categories.findById(CATEGORY_PARENT).orElseThrow();
        var child = categories.findById(CATEGORY_CHILD).orElseThrow();
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.setStatisticsEnabled(true);
        statistics.clear();

        try {
            var result = categoryService.getAllCategories();
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
            assertThat(result).filteredOn(value -> value.getId().equals(child.getId())).singleElement()
                    .satisfies(value -> assertThat(value.getParentId()).isEqualTo(parent.getId()));
        } finally {
            statistics.setStatisticsEnabled(false);
        }
        assertThat(categories.findByParentIdAndNameStartingWith(parent.getId(), "letter"))
              .extracting(Category::getId).containsExactly(child.getId());
    }

    @Test
    void prefixSearchTreatsSqlWildcardsLiterallyAtEveryLevelAndInFavorites() {
        var parent = categories.findById(CATEGORY_PARENT).orElseThrow();
        for (String prefix : List.of("%", "_", "\\")) {
            var root = categories.save(Category.builder().imageUrl("https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/test_image.webp").name(prefix + "Root").build());
            var child = categories.save(Category.builder().imageUrl("https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/test_image.webp").name(prefix + "Child").parent(parent).build());
            var substring = categories.save(Category.builder().imageUrl("https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/test_image.webp").name("X" + prefix + "Child").parent(parent).build());

            categoryService.saveFavorite(TestIds.USER_ANA, root.getId());
            categoryService.saveFavorite(TestIds.USER_ANA, child.getId());
            categoryService.saveFavorite(TestIds.USER_ANA, substring.getId());

            assertThat(categoryService.getCategoriesByParentIdAndName(null, prefix))
                    .extracting(CategoryDto::getId).containsExactly(root.getId());
            assertThat(categoryService.getCategoriesByParentIdAndName(parent.getId(), prefix))
                    .extracting(CategoryDto::getId).containsExactly(child.getId());
            assertThat(categoryService.getFavoritesByName(TestIds.USER_ANA, prefix))
                    .extracting(CategoryDto::getId).containsExactly(child.getId(), root.getId());
            assertThat(categoryService.getFavoritesByName(TestIds.USER_DAN, prefix)).isEmpty();
        }
    }

    @Test
    void databaseRejectsDanglingForeignKeys() {
        var invalid = Category.builder().imageUrl("https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/test_image.webp").name("Invalid")
              .parent(Category.builder().id(id(999999)).build()).build();

        assertThatThrownBy(() -> categories.save(invalid)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
