package com.project.ohl;

import com.project.ohl.application.model.ClassroomDto;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.ClassroomService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.domain.exception.InvalidRequestException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.INSTITUTION_A;
import static com.project.ohl.TestIds.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SchemaNormalizationTest extends DatabaseIntegrationTest {
    @Autowired CategoryService categories;
    @Autowired UserService users;
    @Autowired ClassroomService classrooms;
    @Autowired JdbcTemplate database;

    private UUID user(String name) { return database.queryForObject("SELECT id FROM users WHERE name = ?", UUID.class, name); }

    @Test void favoritesAreIndependentAndRepeatedSavesDoNotDuplicateThem() {
        UUID ana = user("Ana"), dan = user("Dan");
        assertThat(categories.getFavoriteCategories(ana)).extracting(value -> value.getId()).containsExactly(CATEGORY_CHILD);
        assertThat(categories.getFavoriteCategories(dan)).extracting(value -> value.getId()).containsExactly(CATEGORY_PARENT);
        categories.deleteFavorite(ana, CATEGORY_CHILD);
        categories.saveFavorite(ana, CATEGORY_PARENT);
        categories.saveFavorite(ana, CATEGORY_PARENT);
        assertThat(categories.getFavoriteCategories(ana)).hasSize(1);
        categories.deleteFavorite(ana, CATEGORY_PARENT);
        assertThat(categories.getCategoryById(CATEGORY_PARENT, ana).getIsFavorite()).isFalse();
        assertThat(categories.getCategoryById(CATEGORY_PARENT, dan).getIsFavorite()).isTrue();
    }

    @Test void renamingCategoriesPreservesTheirUuidChildrenAndMediaKeys() {
        var parent = categories.getCategoryById(CATEGORY_PARENT);
        parent.setName("Translated alphabet"); parent.setMediaKey(null);
        var changed = categories.updateCategoryById(parent, parent.getId());
        assertThat(changed.getId()).isEqualTo(CATEGORY_PARENT);
        assertThat(changed.getMediaKey()).isEqualTo("alfabet");
        assertThat(categories.getCategoriesByParentIdAndSectionIdAndName(CATEGORY_PARENT, parent.getSectionId(), ""))
                .extracting(value -> value.getId()).containsExactly(CATEGORY_CHILD);
    }


    @Test void parentAndChildMustBelongToTheSameSection() {
        var child = categories.getCategoryById(CATEGORY_CHILD);
        UUID otherSection = database.queryForObject("SELECT id FROM sections WHERE code = 'EXPRESSIONS'", UUID.class);
        child.setSectionId(otherSection);
        assertThatThrownBy(() -> categories.updateCategoryById(child, child.getId())).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> database.update("UPDATE categories SET parent_id = id WHERE id = ?", CATEGORY_PARENT))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void accountEmailsAreNormalizedAndCannotBeDuplicated() {
        UUID role = ROLE_USER;
        var request = UserDto.builder().name("Created user").email("  CREATED@Example.TEST ")
                .password("test-password").mobile("0712345678").address("Bucharest")
                .institutionId(INSTITUTION_A).roleId(role).build();
        assertThat(users.saveUser(request).getEmail()).isEqualTo("created@example.test");
        request.setEmail(" ANA@example.TEST ");
        assertThatThrownBy(() -> users.saveUser(request)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void classroomsHaveSchoolMembershipAndUsersCannotJoinAnotherSchoolsClassroom() {
        var created = classrooms.saveClassroom(ClassroomDto.builder().name("Class C").institutionId(INSTITUTION_A).build());
        assertThat(classrooms.getClassroomById(created.getId()).getInstitutionId()).isEqualTo(INSTITUTION_A);
        var ana = users.getUserById(user("Ana"));
        ana.setPassword("test-password");
        ana.setClassroomId(database.queryForObject("SELECT id FROM classrooms WHERE name = 'B'", UUID.class));
        assertThatThrownBy(() -> users.updateUserById(ana, ana.getId())).isInstanceOf(InvalidRequestException.class);
    }
}
