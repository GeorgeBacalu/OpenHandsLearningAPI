package com.project.ohl.adapter.config;

import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.FeedbackService;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.in.LetterSignPairService;
import com.project.ohl.application.port.in.NotificationService;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.application.port.out.LetterSignPairRepositoryPort;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.application.service.CategoryServiceImpl;
import com.project.ohl.application.service.FeedbackServiceImpl;
import com.project.ohl.application.service.InstitutionServiceImpl;
import com.project.ohl.application.service.LetterSignPairServiceImpl;
import com.project.ohl.application.service.NotificationServiceImpl;
import com.project.ohl.application.service.RoleServiceImpl;
import com.project.ohl.application.service.SectionServiceImpl;
import com.project.ohl.application.service.UserServiceImpl;
import com.project.ohl.application.port.out.PasswordHasher;
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
    public SectionService sectionService(SectionRepositoryPort sections) {
        return transactional(SectionService.class, new SectionServiceImpl(sections));
    }

    @Bean
    public InstitutionService institutionService(InstitutionRepositoryPort institutions) {
        return transactional(InstitutionService.class, new InstitutionServiceImpl(institutions));
    }

    @Bean
    public RoleService roleService(RoleRepositoryPort roles) {
        return transactional(RoleService.class, new RoleServiceImpl(roles));
    }

    @Bean
    public LetterSignPairService letterSignPairService(LetterSignPairRepositoryPort letters) {
        return transactional(LetterSignPairService.class, new LetterSignPairServiceImpl(letters));
    }

    @Bean
    public CategoryService categoryService(CategoryRepositoryPort categories, SectionService sections) {
        return transactional(CategoryService.class, new CategoryServiceImpl(categories, sections));
    }

    @Bean
    public UserService userService(UserRepositoryPort users, InstitutionService institutions, RoleService roles, PasswordHasher passwordHasher) {
        return transactional(UserService.class, new UserServiceImpl(users, institutions, roles, passwordHasher));
    }

    @Bean
    public FeedbackService feedbackService(FeedbackRepositoryPort feedbacks, UserService users, Clock clock) {
        return transactional(FeedbackService.class, new FeedbackServiceImpl(feedbacks, users, clock));
    }

    @Bean
    public NotificationService notificationService(NotificationRepositoryPort notifications, UserService users, Clock clock) {
        return transactional(NotificationService.class, new NotificationServiceImpl(notifications, users, clock));
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
