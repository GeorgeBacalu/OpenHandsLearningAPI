package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.School;
import com.project.ohl.domain.model.Role;
import java.util.UUID;
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
    School school(UUID id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Role role(UUID id);

}
