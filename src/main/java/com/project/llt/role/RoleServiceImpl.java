package com.project.llt.role;

import com.project.llt.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.project.llt.constants.ExceptionMessageConstants.ROLE_NOT_FOUND;
import static com.project.llt.mapper.RoleMapper.convertToDto;
import static com.project.llt.mapper.RoleMapper.convertToEntity;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;
    private final ModelMapper modelMapper;

    @Override
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleDao.findAll();
        return !roles.isEmpty() ? roles.stream().map(role -> convertToDto(modelMapper, role)).toList() : new ArrayList<>();
    }

    @Override
    public RoleDto getRoleById(Long id) {
        Role role = getRoleEntityById(id);
        return convertToDto(modelMapper, role);
    }

    @Override
    public RoleDto saveRole(RoleDto roleDto) {
        Role role = convertToEntity(modelMapper, roleDto);
        Role savedRole = roleDao.save(role);
        return convertToDto(modelMapper, savedRole);
    }

    @Override
    public RoleDto updateRoleById(RoleDto roleDto, Long id) {
        Role roleToUpdate = getRoleEntityById(id);
        roleToUpdate.setAuthority(roleDto.getAuthority());
        Role updatedRole = roleDao.update(roleToUpdate);
        return convertToDto(modelMapper, updatedRole);
    }

    @Override
    public void deleteRoleById(Long id) {
        Role roleToDelete = getRoleEntityById(id);
        roleDao.delete(roleToDelete);
    }

    @Override
    public Role getRoleEntityById(Long id) {
        return roleDao.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(ROLE_NOT_FOUND, id)));
    }
}
