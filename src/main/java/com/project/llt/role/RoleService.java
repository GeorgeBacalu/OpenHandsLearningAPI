package com.project.llt.role;

import java.util.List;

public interface RoleService {

    List<RoleDto> getAllRoles();

    RoleDto getRoleById(Long id);

    RoleDto saveRole(RoleDto roleDto);

    RoleDto updateRoleById(RoleDto roleDto, Long id);

    void deleteRoleById(Long id);

    Role getRoleEntityById(Long id);
}
