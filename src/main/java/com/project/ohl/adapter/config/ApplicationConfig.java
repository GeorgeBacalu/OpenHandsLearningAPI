package com.project.ohl.adapter.config;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.SchoolMapper;
import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.SchoolService;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.in.MediaService;
import com.project.ohl.application.port.out.MediaStoragePort;
import com.project.ohl.application.service.MediaServiceImpl;
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
import com.project.ohl.application.service.AccountServiceImpl;
import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.adapter.transaction.TransactionalAccountService;
import com.project.ohl.adapter.transaction.TransactionalCategoryService;
import com.project.ohl.adapter.transaction.TransactionalRoleService;
import com.project.ohl.adapter.transaction.TransactionalSchoolService;
import com.project.ohl.adapter.transaction.TransactionalUserService;
import com.project.ohl.adapter.transaction.TransactionalSchoolDirectoryService;
import com.project.ohl.application.port.in.SchoolDirectoryService;
import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.application.service.SchoolDirectoryServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfig {
    @Bean
    public SchoolDirectoryService schoolDirectoryService(SchoolDirectoryPort directory) {
        return new TransactionalSchoolDirectoryService(new SchoolDirectoryServiceImpl(directory));
    }

    @Bean
    public MediaService mediaService(MediaStoragePort storage,
            @org.springframework.beans.factory.annotation.Value("${app.storage.s3.environment:dev}") String environment) {
        return new MediaServiceImpl(storage, environment);
    }

    @Bean
    public SectionService sectionService(MediaService media) {
        return new SectionServiceImpl(media);
    }

    @Bean
    public SchoolService schoolService(SchoolRepositoryPort schools, SchoolMapper mapper) {
        return new TransactionalSchoolService(new SchoolServiceImpl(schools, mapper));
    }

    @Bean
    public RoleService roleService(RoleRepositoryPort roles, RoleMapper mapper) {
        return new TransactionalRoleService(new RoleServiceImpl(roles, mapper));
    }

    @Bean
    public CategoryService categoryService(CategoryRepositoryPort categories, CategoryMapper mapper, FavoriteRepositoryPort favorites, UserRepositoryPort users) {
        return new TransactionalCategoryService(new CategoryServiceImpl(categories, mapper, favorites, users));
    }

    @Bean
    public UserService userService(UserRepositoryPort users, SchoolService schools, RoleService roles, PasswordHasher passwordHasher, UserMapper mapper) {
        return new TransactionalUserService(new UserServiceImpl(users, schools, roles, passwordHasher, mapper));
    }

    @Bean
    public AccountService accountService(UserRepositoryPort users, RoleRepositoryPort roles,
            SchoolRepositoryPort schools, PasswordHasher passwords) {
        return new TransactionalAccountService(new AccountServiceImpl(users, roles, schools, passwords));
    }
}
