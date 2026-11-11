package com.project.ohl.adapter.config;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.ClassroomMapper;
import com.project.ohl.application.mapper.FeedbackMapper;
import com.project.ohl.application.mapper.InstitutionMapper;
import com.project.ohl.application.mapper.LetterSignPairMapper;
import com.project.ohl.application.mapper.NotificationMapper;
import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.mapper.SectionMapper;
import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.ClassroomService;
import com.project.ohl.application.port.in.FeedbackService;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.in.LetterSignPairService;
import com.project.ohl.application.port.in.NotificationService;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.ClassroomRepositoryPort;
import com.project.ohl.application.port.out.FavoriteRepositoryPort;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.application.port.out.LetterSignPairRepositoryPort;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.application.service.CategoryServiceImpl;
import com.project.ohl.application.service.ClassroomServiceImpl;
import com.project.ohl.application.service.FeedbackServiceImpl;
import com.project.ohl.application.service.InstitutionServiceImpl;
import com.project.ohl.application.service.LetterSignPairServiceImpl;
import com.project.ohl.application.service.NotificationServiceImpl;
import com.project.ohl.application.service.RoleServiceImpl;
import com.project.ohl.application.service.SectionServiceImpl;
import com.project.ohl.application.service.UserServiceImpl;
import java.time.Clock;
import java.util.Objects;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfig {
    private final PlatformTransactionManager transactions;

    public ApplicationConfig(PlatformTransactionManager transactions) {
        this.transactions = transactions;
    }

    @Bean
    public ClassroomService classroomService(ClassroomRepositoryPort classrooms, ClassroomMapper mapper) {
        return transactional(ClassroomService.class, new ClassroomServiceImpl(classrooms, mapper));
    }

    @Bean
    public SectionService sectionService(SectionRepositoryPort sections, SectionMapper mapper) {
        return transactional(SectionService.class, new SectionServiceImpl(sections, mapper));
    }

    @Bean
    public InstitutionService institutionService(InstitutionRepositoryPort institutions, InstitutionMapper mapper) {
        return transactional(InstitutionService.class, new InstitutionServiceImpl(institutions, mapper));
    }

    @Bean
    public RoleService roleService(RoleRepositoryPort roles, RoleMapper mapper) {
        return transactional(RoleService.class, new RoleServiceImpl(roles, mapper));
    }

    @Bean
    public LetterSignPairService letterSignPairService(LetterSignPairRepositoryPort letters, LetterSignPairMapper mapper) {
        return transactional(LetterSignPairService.class, new LetterSignPairServiceImpl(letters, mapper));
    }

    @Bean
    public CategoryService categoryService(CategoryRepositoryPort categories, SectionService sections, CategoryMapper mapper, FavoriteRepositoryPort favorites, UserRepositoryPort users) {
        return transactional(CategoryService.class, new CategoryServiceImpl(categories, sections, mapper, favorites, users));
    }

    @Bean
    public UserService userService(UserRepositoryPort users, InstitutionService institutions, RoleService roles, PasswordHasher passwordHasher, UserMapper mapper, ClassroomRepositoryPort classrooms) {
        return transactional(UserService.class, new UserServiceImpl(users, institutions, roles, passwordHasher, mapper, classrooms));
    }

    @Bean
    public FeedbackService feedbackService(FeedbackRepositoryPort feedbacks, UserService users, Clock clock, FeedbackMapper mapper) {
        return transactional(FeedbackService.class, new FeedbackServiceImpl(feedbacks, users, clock, mapper));
    }

    @Bean
    public NotificationService notificationService(NotificationRepositoryPort notifications, UserService users, Clock clock, NotificationMapper mapper) {
        return transactional(NotificationService.class, new NotificationServiceImpl(notifications, users, clock, mapper));
    }

    private <T> T transactional(Class<T> port, T target) {
        var attributes = new Properties();
        attributes.setProperty("*", "PROPAGATION_REQUIRED,readOnly");
        attributes.setProperty("save*", "PROPAGATION_REQUIRED");
        attributes.setProperty("update*", "PROPAGATION_REQUIRED");
        attributes.setProperty("delete*", "PROPAGATION_REQUIRED");
        var proxy = new TransactionProxyFactoryBean();
        proxy.setTarget(target);
        proxy.setProxyInterfaces(new Class<?>[]{port});
        proxy.setTransactionManager(transactions);
        proxy.setTransactionAttributes(attributes);
        proxy.afterPropertiesSet();
        return port.cast(Objects.requireNonNull(proxy.getObject()));
    }
}
