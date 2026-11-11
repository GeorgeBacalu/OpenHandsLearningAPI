package com.project.ohl.application.service;

import com.project.ohl.application.model.RoleDto;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.domain.model.Role;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.ROLE_NOT_FOUND;
import static com.project.ohl.application.mapper.RoleMapper.convertToDto;
import static com.project.ohl.application.mapper.RoleMapper.convertToEntity;

@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepositoryPort roleRepository;

    @Override
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(role -> convertToDto(role)).toList();
    }

    @Override
    public RoleDto getRoleById(Long id) {
        Role role = getRoleEntityById(id);
        return convertToDto(role);
    }

    @Override
    public RoleDto saveRole(RoleDto roleDto) {
        Role role = convertToEntity(roleDto);
        Role savedRole = roleRepository.save(role);
        return convertToDto(savedRole);
    }

    @Override
    public RoleDto updateRoleById(RoleDto roleDto, Long id) {
        Role roleToUpdate = getRoleEntityById(id);
        roleToUpdate.setAuthority(roleDto.getAuthority());
        Role updatedRole = roleRepository.update(roleToUpdate);
        return convertToDto(updatedRole);
    }

    @Override
    public void deleteRoleById(Long id) {
        Role roleToDelete = getRoleEntityById(id);
        roleRepository.delete(roleToDelete);
    }

    @Override
    public Role getRoleEntityById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(ROLE_NOT_FOUND, id)));
    }
}
