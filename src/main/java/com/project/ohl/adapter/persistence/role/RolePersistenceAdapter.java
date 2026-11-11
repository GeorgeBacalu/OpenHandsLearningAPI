package com.project.ohl.adapter.persistence.role;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.domain.model.Role;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class RolePersistenceAdapter extends JpaPersistenceAdapter<Role, RoleEntity> implements RoleRepositoryPort {

    public RolePersistenceAdapter(RoleJpaRepository repository) {
        super(repository);
    }

    @Override
    protected RoleEntity newEntity() {
        return new RoleEntity();
    }

    @Override
    protected Long idOf(Role model) {
        return model.getId();
    }

    @Override
    protected Role toDomain(RoleEntity entity) {
        return Role.builder().id(entity.getId())
              .authority(entity.getAuthority())
              .build();
    }

    @Override
    protected void apply(Role model, RoleEntity entity) {
        entity.setAuthority(model.getAuthority());
    }
}
