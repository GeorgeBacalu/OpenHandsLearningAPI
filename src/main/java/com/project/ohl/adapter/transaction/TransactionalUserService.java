package com.project.ohl.adapter.transaction;

import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.domain.model.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionalUserService implements UserService {
    private final UserService delegate;

    @Override
    public List<UserDto> getAllUsers() {
        return delegate.getAllUsers();
    }

    @Override
    public UserDto getUserById(UUID id) {
        return delegate.getUserById(id);
    }

    @Override
    @Transactional
    public UserDto saveUser(UserDto userDto) {
        return delegate.saveUser(userDto);
    }

    @Override
    @Transactional
    public UserDto updateUserById(UserDto userDto, UUID id) {
        return delegate.updateUserById(userDto, id);
    }

    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        delegate.deleteUserById(id);
    }

    @Override
    public User getUserEntityById(UUID id) {
        return delegate.getUserEntityById(id);
    }
}
