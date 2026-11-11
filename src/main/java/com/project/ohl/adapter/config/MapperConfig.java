package com.project.ohl.adapter.config;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.FeedbackMapper;
import com.project.ohl.application.mapper.InstitutionMapper;
import com.project.ohl.application.mapper.LetterSignPairMapper;
import com.project.ohl.application.mapper.NotificationMapper;
import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.mapper.SectionMapper;
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
    public FeedbackMapper feedbackMapper() {
        return Mappers.getMapper(FeedbackMapper.class);
    }

    @Bean
    public InstitutionMapper institutionMapper() {
        return Mappers.getMapper(InstitutionMapper.class);
    }

    @Bean
    public LetterSignPairMapper letterSignPairMapper() {
        return Mappers.getMapper(LetterSignPairMapper.class);
    }

    @Bean
    public NotificationMapper notificationMapper() {
        return Mappers.getMapper(NotificationMapper.class);
    }

    @Bean
    public RoleMapper roleMapper() {
        return Mappers.getMapper(RoleMapper.class);
    }

    @Bean
    public SectionMapper sectionMapper() {
        return Mappers.getMapper(SectionMapper.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }
}
