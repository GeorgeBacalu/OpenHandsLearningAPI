package com.project.ohl.application.port.in;

import com.project.ohl.application.model.RoleDto;
import com.project.ohl.domain.model.Role;

import java.util.List;

public interface RoleService {

    List<RoleDto> getAllRoles();

    RoleDto getRoleById(Long id);

    RoleDto saveRole(RoleDto roleDto);

    RoleDto updateRoleById(RoleDto roleDto, Long id);

    void deleteRoleById(Long id);

    Role getRoleEntityById(Long id);
}
