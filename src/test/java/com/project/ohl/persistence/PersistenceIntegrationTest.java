package com.project.ohl.persistence;

import com.project.ohl.adapter.persistence.section.SectionEntity;
import com.project.ohl.adapter.persistence.section.SectionJpaRepository;
import com.project.ohl.domain.model.Category;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.domain.model.Feedback;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.domain.model.FeedbackType;
import com.project.ohl.domain.model.Institution;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.domain.model.LetterSignPair;
import com.project.ohl.application.port.out.LetterSignPairRepositoryPort;
import com.project.ohl.domain.model.Notification;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.domain.model.Role;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.domain.model.Authority;
import com.project.ohl.domain.model.Section;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.domain.model.User;
import com.project.ohl.application.port.out.UserRepositoryPort;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PersistenceIntegrationTest {
    @Autowired private SectionRepositoryPort sections;
    @Autowired private InstitutionRepositoryPort institutions;
    @Autowired private RoleRepositoryPort roles;
    @Autowired private UserRepositoryPort users;
    @Autowired private CategoryRepositoryPort categories;
    @Autowired private CategoryService categoryService;
    @Autowired private com.project.ohl.application.mapper.CategoryMapper categoryMapper;
    @Autowired private LetterSignPairRepositoryPort letters;
    @Autowired private FeedbackRepositoryPort feedbacks;
    @Autowired private NotificationRepositoryPort notifications;
    @Autowired private SectionJpaRepository sectionJpaRepository;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @Autowired private com.project.ohl.application.port.in.UserService userService;

    @Test
    void rejectsSelfParentsAndDescendantParents() {
        var section = sections.save(Section.builder().name("Tree").build());
        var parent = categories.save(Category.builder().name("Parent").section(section).build());
        var child = categories.save(Category.builder().name("Child").parent(parent).section(section).build());
        var update = categoryMapper.convertToDto(parent);
        update.setParentId(parent.getId());
        assertThatThrownBy(() -> categoryService.updateCategoryById(update, parent.getId()))
                .isInstanceOf(com.project.ohl.domain.exception.InvalidRequestException.class);
        update.setParentId(child.getId());
        assertThatThrownBy(() -> categoryService.updateCategoryById(update, parent.getId()))
                .isInstanceOf(com.project.ohl.domain.exception.InvalidRequestException.class);
    }

    @Test
    void hashesPasswordsBeforeSavingUsers() {
        var institution = institutions.save(Institution.builder().school("School").classroom("B").build());
        var role = roles.save(Role.builder().authority(Authority.USER).build());
        var request = com.project.ohl.application.model.UserDto.builder().name("Ana")
                .email("hashed@example.test").password("long-test-password").mobile("0712345678")
                .address("Bucharest").institutionId(institution.getId()).roleId(role.getId()).build();
        var created = userService.saveUser(request);
        var stored = users.findById(created.getId()).orElseThrow().getPassword();
        assertThat(stored).startsWith("$argon2id$").doesNotContain(request.getPassword());
        var encoder = new org.springframework.security.crypto.argon2.Argon2PasswordEncoder(16, 32, 1, 19456, 2);
        assertThat(encoder.matches(request.getPassword(), stored)).isTrue();
        request.setPassword("another-test-password");
        userService.updateUserById(request, created.getId());
        var updated = users.findById(created.getId()).orElseThrow().getPassword();
        assertThat(updated).isNotEqualTo(stored);
        assertThat(encoder.matches(request.getPassword(), updated)).isTrue();
    }

    @Test
    void persistsAndUpdatesEveryResourceUsingTheSqlSchema() {
        var section = sections.save(Section.builder().name("Video").iconId(1).imageId(2).build());
        var institution = institutions.save(Institution.builder().school("School").classroom("A").build());
        var role = roles.save(Role.builder().authority(Authority.USER).build());
        var user = users.save(User.builder().name("Ana").email("ana@example.test")
              .password("test-password").mobile("0712345678").address("Bucharest")
              .birthday(LocalDate.of(2000, 1, 1)).institution(institution).role(role).build());
        var parent = categories.save(Category.builder().name("Alphabet").section(section).build());
        var child = categories.save(Category.builder().name("A").parent(parent).section(section).build());
        var letter = letters.save(LetterSignPair.builder().letter("A").imageId(3).build());
        var sentAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        var feedback = feedbacks.save(Feedback.builder().description("Useful lesson")
              .type(FeedbackType.IMPROVEMENT).sentAt(sentAt).user(user).build());
        var notification = notifications.save(Notification.builder().message("Welcome")
              .sender(user).receiver(user).sentAt(sentAt).isRead(false).build());

        assertThat(users.findById(user.getId()).orElseThrow().getInstitution().getId()).isEqualTo(institution.getId());
        assertThat(users.findById(user.getId()).orElseThrow().getRole().getId()).isEqualTo(role.getId());
        assertThat(categories.findById(child.getId()).orElseThrow().getParent().getId()).isEqualTo(parent.getId());
        assertThat(feedbacks.findById(feedback.getId()).orElseThrow().getUser().getId()).isEqualTo(user.getId());
        assertThat(notifications.findById(notification.getId()).orElseThrow().getReceiver().getId()).isEqualTo(user.getId());
        assertThat(letters.findById(letter.getId()).orElseThrow().getLetter()).isEqualTo("A");

        section.setName("Updated video");
        institution.setClassroom("B");
        role.setAuthority(Authority.ADMIN);
        user.setName("Updated Ana");
        child.setIsFavorite(true);
        letter.setLetter("B");
        feedback.setDescription("Updated feedback");
        notification.setIsRead(true);

        assertThat(sections.update(section).getName()).isEqualTo("Updated video");
        assertThat(institutions.update(institution).getClassroom()).isEqualTo("B");
        assertThat(roles.update(role).getAuthority()).isEqualTo(Authority.ADMIN);
        assertThat(users.update(user).getName()).isEqualTo("Updated Ana");
        assertThat(categories.update(child).getIsFavorite()).isTrue();
        assertThat(letters.update(letter).getLetter()).isEqualTo("B");
        assertThat(feedbacks.update(feedback).getDescription()).isEqualTo("Updated feedback");
        assertThat(notifications.update(notification).getIsRead()).isTrue();

        notifications.delete(notification);
        feedbacks.delete(feedback);
        categories.delete(child);
        categories.delete(parent);
        letters.delete(letter);
        users.delete(user);
        roles.delete(role);
        institutions.delete(institution);
        sections.delete(section);
        sectionJpaRepository.flush();
        assertThat(categories.findById(child.getId())).isEmpty();
        assertThat(users.findById(user.getId())).isEmpty();
    }

    @Test
    void categoryListsUseOneQueryAndReturnTheActualParentId() {
        var section = sections.save(Section.builder().name("Video").build());
        var parent = categories.save(Category.builder().name("Alphabet").section(section).build());
        var child = categories.save(Category.builder().name("Letter A").parent(parent).section(section).build());
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var result = categoryService.getAllCategories();

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(result).filteredOn(value -> value.getId().equals(child.getId())).singleElement()
              .satisfies(value -> {
                  assertThat(value.getParentId()).isEqualTo(parent.getId());
                  assertThat(value.getSectionId()).isEqualTo(section.getId());
              });
        assertThat(categories.findByParentIdAndSectionIdAndNameContaining(parent.getId(), section.getId(), "letter"))
              .extracting(Category::getId).containsExactly(child.getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void staleWritesCannotOverwriteAConcurrentUpdate() {
        var created = sections.save(Section.builder().name("Original").build());
        try {
            SectionEntity first = sectionJpaRepository.findById(created.getId()).orElseThrow();
            SectionEntity stale = sectionJpaRepository.findById(created.getId()).orElseThrow();
            first.setName("First change");
            sectionJpaRepository.saveAndFlush(first);
            stale.setName("Stale change");

            assertThatThrownBy(() -> sectionJpaRepository.saveAndFlush(stale))
                  .isInstanceOf(ObjectOptimisticLockingFailureException.class);
            assertThat(sections.findById(created.getId()).orElseThrow().getName()).isEqualTo("First change");
        } finally {
            sections.delete(created);
        }
    }

    @Test
    void databaseRejectsDanglingForeignKeys() {
        var invalid = Category.builder().name("Invalid")
              .section(Section.builder().id(Long.MAX_VALUE).build()).build();

        assertThatThrownBy(() -> categories.save(invalid)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
