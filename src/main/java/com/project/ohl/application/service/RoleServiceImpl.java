package com.project.ohl.application.service;

import java.util.UUID;
import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.model.RoleDto;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.domain.model.Role;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.ROLE_NOT_FOUND;

@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepositoryPort roleRepository;

    private final RoleMapper mapper;

    @Override
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public RoleDto getRoleById(UUID id) {
        Role role = getRoleEntityById(id);
        return mapper.convertToDto(role);
    }

    @Override
    public RoleDto saveRole(RoleDto roleDto) {
        Role role = mapper.convertToEntity(roleDto);
        Role savedRole = roleRepository.save(role);
        return mapper.convertToDto(savedRole);
    }

    @Override
    public RoleDto updateRoleById(RoleDto roleDto, UUID id) {
        Role roleToUpdate = getRoleEntityById(id);
        roleToUpdate.setAuthority(roleDto.getAuthority());
        Role updatedRole = roleRepository.update(roleToUpdate);
        return mapper.convertToDto(updatedRole);
    }

    @Override
    public void deleteRoleById(UUID id) {
        Role roleToDelete = getRoleEntityById(id);
        roleRepository.delete(roleToDelete);
    }

    @Override
    public Role getRoleEntityById(UUID id) {
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(ROLE_NOT_FOUND, id)));
    }
}
