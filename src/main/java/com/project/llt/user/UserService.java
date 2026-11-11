package com.project.llt.user;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto saveUser(UserDto userDto);

    UserDto updateUserById(UserDto userDto, Long id);

    void deleteUserById(Long id);

    User getUserEntityById(Long id);
}
