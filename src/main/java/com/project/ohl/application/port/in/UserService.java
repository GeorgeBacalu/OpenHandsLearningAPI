package com.project.ohl.application.port.in;

import java.util.UUID;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.domain.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(UUID id);

    UserDto saveUser(UserDto userDto);

    UserDto updateUserById(UserDto userDto, UUID id);

    void deleteUserById(UUID id);

    User getUserEntityById(UUID id);
}
