package com.project.ohl.adapter.transaction;

import com.project.ohl.application.model.RoleDto;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.domain.model.Role;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionalRoleService implements RoleService {
    private final RoleService delegate;

    @Override
    public List<RoleDto> getAllRoles() {
        return delegate.getAllRoles();
    }

    @Override
    public RoleDto getRoleById(UUID id) {
        return delegate.getRoleById(id);
    }

    @Override
    @Transactional
    public RoleDto saveRole(RoleDto roleDto) {
        return delegate.saveRole(roleDto);
    }

    @Override
    @Transactional
    public RoleDto updateRoleById(RoleDto roleDto, UUID id) {
        return delegate.updateRoleById(roleDto, id);
    }

    @Override
    @Transactional
    public void deleteRoleById(UUID id) {
        delegate.deleteRoleById(id);
    }

    @Override
    public Role getRoleEntityById(UUID id) {
        return delegate.getRoleEntityById(id);
    }
}
