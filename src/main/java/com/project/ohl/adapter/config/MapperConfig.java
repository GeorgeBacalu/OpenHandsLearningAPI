package com.project.ohl.adapter.config;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.SchoolMapper;
import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MapperConfig {
    @Bean
    public CategoryMapper categoryMapper() {
        return Mappers.getMapper(CategoryMapper.class);
    }

    @Bean
    public SchoolMapper schoolMapper() {
        return Mappers.getMapper(SchoolMapper.class);
    }

    @Bean
    public RoleMapper roleMapper() {
        return Mappers.getMapper(RoleMapper.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }
}
