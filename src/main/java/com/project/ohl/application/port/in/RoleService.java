package com.project.ohl.application.port.in;

import java.util.UUID;
import com.project.ohl.application.model.RoleDto;
import com.project.ohl.domain.model.Role;

import java.util.List;

public interface RoleService {

    List<RoleDto> getAllRoles();

    RoleDto getRoleById(UUID id);

    RoleDto saveRole(RoleDto roleDto);

    RoleDto updateRoleById(RoleDto roleDto, UUID id);

    void deleteRoleById(UUID id);

    Role getRoleEntityById(UUID id);
}
