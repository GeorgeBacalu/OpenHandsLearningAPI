package com.project.ohl;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Role;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SCHOOL_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SchemaNormalizationTest extends DatabaseIntegrationTest {
    @Autowired
    CategoryService categories;

    @Autowired
    UserService users;

    @Autowired
    JdbcTemplate database;

    private UUID user(String name) { return database.queryForObject("SELECT id FROM users WHERE name = ?", UUID.class, name); }

    @Test void expressionsCanBecomeChildrenWithoutChangingTheirIdentityOrFavorites() {
        UUID parentId = UUID.randomUUID();

        database.update("INSERT INTO categories (id, name, parent_id, image_url) VALUES (?, ?, NULL, ?)",
                parentId, "Expresii", "https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/expressions_section.webp");

        var parent = categories.getCategoryById(parentId);
        var child = categories.getCategoryById(CATEGORY_CHILD);

        String imageUrl = child.getImageUrl();
        child.setParentId(parent.getId());

        var moved = categories.updateCategoryById(child, child.getId());

        assertThat(moved.getId()).isEqualTo(CATEGORY_CHILD);
        assertThat(moved.getImageUrl()).isEqualTo(imageUrl);
        assertThat(categories.getCategoriesByParentIdAndName(null, ""))
                .extracting(CategoryDto::getName).containsExactly("Alphabet", "Expresii");
        assertThat(categories.getCategoriesByParentIdAndName(parent.getId(), ""))
                .extracting(CategoryDto::getId).containsExactly(CATEGORY_CHILD);
        assertThat(categories.getFavoriteCategories(user("Ana")))
                .extracting(CategoryDto::getId).containsExactly(CATEGORY_CHILD);
    }

    @Test void favoritesKeepOnlyTheirUserAndCategoryPair() {
        var columns = database.queryForList("SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'favorites'", String.class);

        assertThat(columns).containsExactlyInAnyOrder("user_id", "category_id");
        assertThat(database.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'user_favorites'", Long.class)).isZero();
        assertThat(database.queryForObject("SELECT COUNT(*) FROM favorites", Long.class)).isEqualTo(2L);
    }

    @Test void favoritesAreIndependentAndRepeatedSavesDoNotDuplicateThem() {
        UUID ana = user("Ana"), dan = user("Dan");

        assertThat(categories.getFavoriteCategories(ana)).extracting(CategoryDto::getId).containsExactly(CATEGORY_CHILD);
        assertThat(categories.getFavoriteCategories(dan)).extracting(CategoryDto::getId).containsExactly(CATEGORY_PARENT);

        categories.deleteFavorite(ana, CATEGORY_CHILD);
        categories.saveFavorite(ana, CATEGORY_PARENT);
        categories.saveFavorite(ana, CATEGORY_PARENT);

        assertThat(categories.getFavoriteCategories(ana)).hasSize(1);

        categories.deleteFavorite(ana, CATEGORY_PARENT);

        assertThat(categories.getCategoryById(CATEGORY_PARENT, ana).getIsFavorite()).isFalse();
        assertThat(categories.getCategoryById(CATEGORY_PARENT, dan).getIsFavorite()).isTrue();
    }

    @Test void renamingCategoriesPreservesTheirUuidChildrenAndImageUrls() {
        var parent = categories.getCategoryById(CATEGORY_PARENT);
        parent.setName("Translated alphabet"); parent.setImageUrl(null);

        var changed = categories.updateCategoryById(parent, parent.getId());

        assertThat(changed.getId()).isEqualTo(CATEGORY_PARENT);
        assertThat(changed.getImageUrl()).isEqualTo("https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/main.webp");
        assertThat(categories.getCategoriesByParentIdAndName(CATEGORY_PARENT, ""))
                .extracting(CategoryDto::getId).containsExactly(CATEGORY_CHILD);
    }

    @Test void categoriesHaveNoSectionReferenceAndRejectCycles() {
        var columns = database.queryForList("SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'categories'", String.class);
        assertThat(columns).containsExactlyInAnyOrder("id", "name", "parent_id", "image_url", "video_url");

        var parent = categories.getCategoryById(CATEGORY_PARENT);
        parent.setParentId(CATEGORY_CHILD);

        assertThatThrownBy(() -> categories.updateCategoryById(parent, parent.getId())).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> database.update("UPDATE categories SET parent_id = id WHERE id = ?", CATEGORY_PARENT))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void userRolesAreStoredAsConstrainedStrings() {
        assertThat(database.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'roles'", Long.class)).isZero();
        assertThat(database.queryForList("SELECT role FROM users ORDER BY role", String.class))
                .containsExactly("ADMIN", "USER");
        assertThatThrownBy(() -> database.update("UPDATE users SET role = 'OWNER'"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> database.update("UPDATE users SET role = NULL"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void accountEmailsAreNormalizedAndCannotBeDuplicated() {
        var request = UserDto.builder().name("Created user").email("  CREATED@Example.TEST ")
                .password("test-password").phone("0712345678")
                .schoolId(SCHOOL_A).role(Role.USER).build();

        assertThat(users.saveUser(request).getEmail()).isEqualTo("created@example.test");

        request.setEmail(" ANA@example.TEST ");

        assertThatThrownBy(() -> users.saveUser(request)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
