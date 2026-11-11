package com.project.ohl.application.port.in;

import com.project.ohl.application.model.UserDto;
import com.project.ohl.domain.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto saveUser(UserDto userDto);

    UserDto updateUserById(UserDto userDto, Long id);

    void deleteUserById(Long id);

    User getUserEntityById(Long id);
}
