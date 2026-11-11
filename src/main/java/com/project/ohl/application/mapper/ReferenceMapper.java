package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.Institution;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.Section;
import com.project.ohl.domain.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class)
public interface ReferenceMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Category category(Long id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Institution institution(Long id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Role role(Long id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Section section(Long id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    User user(Long id);
}
