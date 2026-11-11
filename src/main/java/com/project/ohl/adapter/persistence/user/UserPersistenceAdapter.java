package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.domain.model.Institution;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.User;
import com.project.ohl.application.port.out.UserRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class UserPersistenceAdapter extends JpaPersistenceAdapter<User, UserEntity> implements UserRepositoryPort {

    public UserPersistenceAdapter(UserJpaRepository repository) {
        super(repository);
    }

    @Override
    protected UserEntity newEntity() {
        return new UserEntity();
    }

    @Override
    protected Long idOf(User model) {
        return model.getId();
    }

    @Override
    protected User toDomain(UserEntity entity) {
        return User.builder().id(entity.getId())
              .name(entity.getName())
              .email(entity.getEmail())
              .password(entity.getPassword())
              .mobile(entity.getMobile())
              .address(entity.getAddress())
              .birthday(entity.getBirthday())
              .institution(entity.getInstitutionId() == null ? null : Institution.builder().id(entity.getInstitutionId()).build())
              .role(entity.getRoleId() == null ? null : Role.builder().id(entity.getRoleId()).build())
              .build();
    }

    @Override
    protected void apply(User model, UserEntity entity) {
        entity.setName(model.getName());
        entity.setEmail(model.getEmail());
        entity.setPassword(model.getPassword());
        entity.setMobile(model.getMobile());
        entity.setAddress(model.getAddress());
        entity.setBirthday(model.getBirthday());
        entity.setInstitutionId(model.getInstitution() == null ? null : model.getInstitution().getId());
        entity.setRoleId(model.getRole() == null ? null : model.getRole().getId());
    }
}
