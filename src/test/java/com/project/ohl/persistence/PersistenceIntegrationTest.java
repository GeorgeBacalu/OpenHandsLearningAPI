package com.project.ohl.persistence;

import com.project.ohl.DatabaseIntegrationTest;
import com.project.ohl.adapter.persistence.section.SectionEntity;
import com.project.ohl.adapter.persistence.section.SectionJpaRepository;
import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.application.port.out.LetterSignPairRepositoryPort;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Authority;
import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.Feedback;
import com.project.ohl.domain.model.FeedbackType;
import com.project.ohl.domain.model.Institution;
import com.project.ohl.domain.model.LetterSignPair;
import com.project.ohl.domain.model.Notification;
import com.project.ohl.domain.model.Section;
import com.project.ohl.domain.model.User;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.INSTITUTION_A;
import static com.project.ohl.TestIds.ROLE_USER;
import static com.project.ohl.TestIds.SECTION_VIDEO;
import static com.project.ohl.TestIds.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PersistenceIntegrationTest extends DatabaseIntegrationTest {
    @Autowired private SectionRepositoryPort sections;
    @Autowired private InstitutionRepositoryPort institutions;
    @Autowired private RoleRepositoryPort roles;
    @Autowired private UserRepositoryPort users;
    @Autowired private CategoryRepositoryPort categories;
    @Autowired private CategoryService categoryService;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private LetterSignPairRepositoryPort letters;
    @Autowired private FeedbackRepositoryPort feedbacks;
    @Autowired private NotificationRepositoryPort notifications;
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
        var institution = institutions.findById(INSTITUTION_A).orElseThrow();
        var role = roles.findById(ROLE_USER).orElseThrow();
        var request = UserDto.builder().name("Ana")
                .email("hashed@example.test").password("long-test-password").mobile("0712345678")
                .address("Bucharest").institutionId(institution.getId()).roleId(role.getId()).build();
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
        var institution = institutions.save(Institution.builder().school("School").build());
        var role = roles.findById(ROLE_USER).orElseThrow();
        var user = users.save(User.builder().name("Ana").email("created@example.test")
              .password("test-password").mobile("0712345678").address("Bucharest")
              .birthday(LocalDate.of(2000, 1, 1)).institution(institution).role(role).build());
        var parent = categories.findById(CATEGORY_PARENT).orElseThrow();
        var child = categories.save(Category.builder().name("C").parent(parent)
                .section(sections.findById(SECTION_VIDEO).orElseThrow()).build());
        var letter = letters.save(LetterSignPair.builder().letter("X").build());
        var sentAt = Instant.parse("2026-01-01T08:00:00Z");
        var feedback = feedbacks.save(Feedback.builder().description("Useful lesson")
              .type(FeedbackType.IMPROVEMENT).sentAt(sentAt).user(user).build());
        var notification = notifications.save(Notification.builder().message("Welcome")
              .sender(user).receiver(user).sentAt(sentAt).isRead(false).build());

        assertThat(List.of(section.getId(), institution.getId(), role.getId(), user.getId(),
                child.getId(), letter.getId(), feedback.getId(), notification.getId()))
                .allSatisfy(value -> assertThat(value.version()).isEqualTo(4));

        assertThat(users.findById(user.getId()).orElseThrow().getInstitution().getId()).isEqualTo(institution.getId());
        assertThat(users.findById(user.getId()).orElseThrow().getRole().getId()).isEqualTo(role.getId());
        assertThat(categories.findById(child.getId()).orElseThrow().getParent().getId()).isEqualTo(parent.getId());
        assertThat(feedbacks.findById(feedback.getId()).orElseThrow().getUser().getId()).isEqualTo(user.getId());
        assertThat(notifications.findById(notification.getId()).orElseThrow().getReceiver().getId()).isEqualTo(user.getId());
        assertThat(letters.findById(letter.getId()).orElseThrow().getLetter()).isEqualTo("X");

        section.setName("Updated video");
        institution.setSchool("Updated School");
        role.setAuthority(Authority.USER);
        user.setName("Updated Ana");
        child.setName("Updated category");
        letter.setLetter("Y");
        feedback.setDescription("Updated feedback");
        notification.setIsRead(true);

        assertThat(sections.update(section).getName()).isEqualTo("Updated video");
        assertThat(institutions.update(institution).getSchool()).isEqualTo("Updated School");
        assertThat(roles.update(role).getAuthority()).isEqualTo(Authority.USER);
        assertThat(users.update(user).getName()).isEqualTo("Updated Ana");
        assertThat(categories.update(child).getName()).isEqualTo("Updated category");
        assertThat(letters.update(letter).getLetter()).isEqualTo("Y");
        assertThat(feedbacks.update(feedback).getDescription()).isEqualTo("Updated feedback");
        assertThat(notifications.update(notification).getIsRead()).isTrue();

        notifications.delete(notification);
        feedbacks.delete(feedback);
        categories.delete(child);
        letters.delete(letter);
        users.delete(user);
        institutions.delete(institution);
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
                        assertThat(value.getSectionId()).isEqualTo(SECTION_VIDEO);
                    });
        } finally {
            statistics.setStatisticsEnabled(false);
        }
        assertThat(categories.findByParentIdAndSectionIdAndNameContaining(parent.getId(), SECTION_VIDEO, "letter"))
              .extracting(Category::getId).containsExactly(child.getId());
    }

    @Test
    void staleWritesCannotOverwriteAConcurrentUpdate() {
        SectionEntity first = sectionJpaRepository.findById(SECTION_VIDEO).orElseThrow();
        SectionEntity stale = sectionJpaRepository.findById(SECTION_VIDEO).orElseThrow();
        first.setName("First change");
        sectionJpaRepository.saveAndFlush(first);
        stale.setName("Stale change");

        assertThatThrownBy(() -> sectionJpaRepository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(sections.findById(SECTION_VIDEO).orElseThrow().getName()).isEqualTo("First change");
    }

    @Test
    void databaseRejectsDanglingForeignKeys() {
        var invalid = Category.builder().name("Invalid")
              .section(Section.builder().id(id(999999)).build()).build();

        assertThatThrownBy(() -> categories.save(invalid)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
