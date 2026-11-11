package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.classroom.ClassroomEntity;
import com.project.ohl.adapter.persistence.school.SchoolEntity;
import com.project.ohl.adapter.persistence.role.RoleEntity;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.model.School;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.User;
import java.util.Locale;
import java.util.UUID;
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
    protected UUID idOf(User model) {
        return model.getId();
    }

    @Override
    protected User toDomain(UserEntity entity) {
        return User.builder().id(entity.getId())
              .classroomId(entity.getClassroom() == null ? null : entity.getClassroom().getId()).name(entity.getName())
              .email(entity.getEmail())
              .password(entity.getPassword())
              .mobile(entity.getMobile())
              .address(entity.getAddress())
              .birthday(entity.getBirthday())
              .school(entity.getSchool() == null ? null : School.builder().id(entity.getSchool().getId()).build())
              .role(entity.getRole() == null ? null : Role.builder().id(entity.getRole().getId()).build())
              .build();
    }

    @Override
    protected void apply(User model, UserEntity entity) {
        entity.setName(model.getName());
        entity.setEmail(model.getEmail() == null ? null : model.getEmail().strip().toLowerCase(Locale.ROOT));
        entity.setClassroom(reference(ClassroomEntity.class, model.getClassroomId()));
        entity.setPassword(model.getPassword());
        entity.setMobile(model.getMobile());
        entity.setAddress(model.getAddress());
        entity.setBirthday(model.getBirthday());
        entity.setSchool(reference(SchoolEntity.class, model.getSchool() == null ? null : model.getSchool().getId()));
        entity.setRole(reference(RoleEntity.class, model.getRole() == null ? null : model.getRole().getId()));
    }
}
