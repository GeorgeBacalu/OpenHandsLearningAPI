package com.project.ohl.persistence;

import com.project.ohl.DatabaseIntegrationTest;
import com.project.ohl.adapter.persistence.section.SectionJpaRepository;
import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Authority;
import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.School;
import com.project.ohl.domain.model.Section;
import com.project.ohl.domain.model.User;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SCHOOL_A;
import static com.project.ohl.TestIds.ROLE_USER;
import static com.project.ohl.TestIds.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PersistenceIntegrationTest extends DatabaseIntegrationTest {
    @Autowired private SectionRepositoryPort sections;
    @Autowired private SchoolRepositoryPort schools;
    @Autowired private RoleRepositoryPort roles;
    @Autowired private UserRepositoryPort users;
    @Autowired private CategoryRepositoryPort categories;
    @Autowired private CategoryService categoryService;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private SectionJpaRepository sectionJpaRepository;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @Autowired private UserService userService;

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
        var role = roles.findById(ROLE_USER).orElseThrow();
        var request = UserDto.builder().name("Ana")
                .email("hashed@example.test").password("long-test-password").phone("0712345678")
                .schoolId(school.getId()).roleId(role.getId()).build();
        var created = userService.saveUser(request);
        var stored = users.findById(created.getId()).orElseThrow().getPassword();
        assertThat(stored).startsWith("$2b$12$").doesNotContain(request.getPassword());
        var encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches(request.getPassword(), stored)).isTrue();
        request.setPassword("another-test-password");
        userService.updateUserById(request, created.getId());
        var updated = users.findById(created.getId()).orElseThrow().getPassword();
        assertThat(updated).isNotEqualTo(stored);
        assertThat(encoder.matches(request.getPassword(), updated)).isTrue();
    }

    @Test
    void persistsAndUpdatesEveryResourceUsingTheHibernateSchema() {
        var section = sections.save(Section.builder().name("Video").build());
        var school = schools.save(School.builder().name("School").cityId(UUID.fromString("00000000-0000-4000-8000-000000000104")).build());
        var role = roles.findById(ROLE_USER).orElseThrow();
        var user = users.save(User.builder().name("Ana").email("created@example.test")
              .password("test-password").phone("0712345678")
              .school(school).role(role).build());
        var parent = categories.findById(CATEGORY_PARENT).orElseThrow();
        var child = categories.save(Category.builder().name("C").parent(parent).build());

        assertThat(List.of(section.getId(), school.getId(), role.getId(), user.getId(),
                child.getId()))
                .allSatisfy(value -> assertThat(value.version()).isEqualTo(4));

        assertThat(users.findById(user.getId()).orElseThrow().getSchool().getId()).isEqualTo(school.getId());
        assertThat(users.findById(user.getId()).orElseThrow().getRole().getId()).isEqualTo(role.getId());
        assertThat(categories.findById(child.getId()).orElseThrow().getParent().getId()).isEqualTo(parent.getId());

        section.setName("Updated video");
        school.setName("Updated School");
        role.setAuthority(Authority.USER);
        user.setName("Updated Ana");
        child.setName("Updated category");

        assertThat(sections.update(section).getName()).isEqualTo("Updated video");
        assertThat(schools.update(school).getName()).isEqualTo("Updated School");
        assertThat(roles.update(role).getAuthority()).isEqualTo(Authority.USER);
        assertThat(users.update(user).getName()).isEqualTo("Updated Ana");
        assertThat(categories.update(child).getName()).isEqualTo("Updated category");

        categories.delete(child);
        users.delete(user);
        schools.delete(school);
        sections.delete(section);
        sectionJpaRepository.flush();
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
                    .satisfies(value -> {
                        assertThat(value.getParentId()).isEqualTo(parent.getId());
                    });
        } finally {
            statistics.setStatisticsEnabled(false);
        }
        assertThat(categories.findByParentIdAndNameContaining(parent.getId(), "letter"))
              .extracting(Category::getId).containsExactly(child.getId());
    }

    @Test
    void databaseRejectsDanglingForeignKeys() {
        var invalid = Category.builder().name("Invalid")
              .parent(Category.builder().id(id(999999)).build()).build();

        assertThatThrownBy(() -> categories.save(invalid)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
