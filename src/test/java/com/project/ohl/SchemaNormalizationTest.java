package com.project.ohl;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Role;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

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

    private UUID user(String name) {
        return userRepository.findAll().stream().filter(user -> user.getName().equals(name))
                .findFirst().orElseThrow().getId();
    }

    @Test void expressionsCanBecomeChildrenWithoutChangingTheirIdentityOrFavorites() {
        UUID parentId = createCategory("Expresii",
                "https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/expressions_section.webp");

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
        var columns = nativeValues("SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'favorites'", String.class);

        assertThat(columns).containsExactlyInAnyOrder("user_id", "category_id");
        assertThat(schemaCount("SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'user_favorites'")).isZero();
        assertThat(favoriteRepository.count()).isEqualTo(2L);
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
        var columns = nativeValues("SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'categories'", String.class);
        assertThat(columns).containsExactlyInAnyOrder("id", "name", "parent_id", "image_url", "video_url");

        var parent = categories.getCategoryById(CATEGORY_PARENT);
        parent.setParentId(CATEGORY_CHILD);

        assertThatThrownBy(() -> categories.updateCategoryById(parent, parent.getId())).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> executeNative("UPDATE categories SET parent_id = id WHERE id = ?1", CATEGORY_PARENT))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test void userRolesAreStoredAsConstrainedStrings() {
        assertThat(schemaCount("SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'roles'")).isZero();
        assertThat(nativeValues("SELECT role FROM users ORDER BY role", String.class))
                .containsExactly("ADMIN", "USER");
        assertThatThrownBy(() -> executeNative("UPDATE users SET role = 'OWNER'"))
                .isInstanceOfAny(ConstraintViolationException.class, DataException.class);
        assertThatThrownBy(() -> executeNative("UPDATE users SET role = NULL"))
                .isInstanceOf(ConstraintViolationException.class);
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
