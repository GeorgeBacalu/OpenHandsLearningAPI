package com.project.ohl.adapter.config;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.SchoolMapper;
import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.SchoolService;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.FavoriteRepositoryPort;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.application.service.CategoryServiceImpl;
import com.project.ohl.application.service.SchoolServiceImpl;
import com.project.ohl.application.service.RoleServiceImpl;
import com.project.ohl.application.service.SectionServiceImpl;
import com.project.ohl.application.service.UserServiceImpl;
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
    public SectionService sectionService() {
        return new SectionServiceImpl();
    }

    @Bean
    public SchoolService schoolService(SchoolRepositoryPort schools, SchoolMapper mapper) {
        return transactional(SchoolService.class, new SchoolServiceImpl(schools, mapper));
    }

    @Bean
    public RoleService roleService(RoleRepositoryPort roles, RoleMapper mapper) {
        return transactional(RoleService.class, new RoleServiceImpl(roles, mapper));
    }

    @Bean
    public CategoryService categoryService(CategoryRepositoryPort categories, CategoryMapper mapper, FavoriteRepositoryPort favorites, UserRepositoryPort users) {
        return transactional(CategoryService.class, new CategoryServiceImpl(categories, mapper, favorites, users));
    }

    @Bean
    public UserService userService(UserRepositoryPort users, SchoolService schools, RoleService roles, PasswordHasher passwordHasher, UserMapper mapper) {
        return transactional(UserService.class, new UserServiceImpl(users, schools, roles, passwordHasher, mapper));
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
