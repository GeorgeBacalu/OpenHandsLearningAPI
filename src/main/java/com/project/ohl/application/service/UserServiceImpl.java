package com.project.ohl.application.service;

import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.domain.model.User;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.in.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.USER_NOT_FOUND;
import static com.project.ohl.application.mapper.UserMapper.convertToDto;
import static com.project.ohl.application.mapper.UserMapper.convertToEntity;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepository;
    private final InstitutionService institutionService;
    private final RoleService roleService;
    private final PasswordHasher passwordHasher;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> convertToDto(user)).toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = getUserEntityById(id);
        return convertToDto(user);
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        user.setPassword(passwordHasher.hash(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public UserDto updateUserById(UserDto userDto, Long id) {
        User userToUpdate = getUserEntityById(id);
        userToUpdate.setName(userDto.getName());
        userToUpdate.setEmail(userDto.getEmail());
        userToUpdate.setPassword(passwordHasher.hash(userDto.getPassword()));
        userToUpdate.setMobile(userDto.getMobile());
        userToUpdate.setAddress(userDto.getAddress());
        userToUpdate.setBirthday(userDto.getBirthday());
        userToUpdate.setInstitution(institutionService.getInstitutionEntityById(userDto.getInstitutionId()));
        userToUpdate.setRole(roleService.getRoleEntityById(userDto.getRoleId()));
        User updatedUser = userRepository.update(userToUpdate);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUserById(Long id) {
        User userToDelete = getUserEntityById(id);
        userRepository.delete(userToDelete);
    }

    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND, id)));
    }
}
