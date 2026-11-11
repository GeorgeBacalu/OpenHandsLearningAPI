package com.project.ohl.application.mapper;

import java.util.UUID;
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
    Category category(UUID id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Institution institution(UUID id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Role role(UUID id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Section section(UUID id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    User user(UUID id);
}
