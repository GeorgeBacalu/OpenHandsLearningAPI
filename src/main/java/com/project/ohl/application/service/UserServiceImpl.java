package com.project.ohl.application.service;

import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.SchoolService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.ohl.application.error.ExceptionMessageConstants.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepository;
    private final SchoolService schoolService;
    private final PasswordHasher passwordHasher;

    private final UserMapper mapper;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public UserDto getUserById(UUID id) {
        User user = getUserEntityById(id);
        return mapper.convertToDto(user);
    }

    @Override
    @Transactional
    public UserDto saveUser(UserDto userDto) {
        User user = mapper.convertToEntity(userDto);
        user.setPasswordHash(passwordHasher.hash(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return mapper.convertToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUserById(UserDto userDto, UUID id) {
        User userToUpdate = getUserEntityById(id);
        userToUpdate.setName(userDto.getName());
        userToUpdate.setEmail(userDto.getEmail());
        userToUpdate.setPasswordHash(passwordHasher.hash(userDto.getPassword()));
        userToUpdate.setPhone(userDto.getPhone());
        userToUpdate.setSchool(schoolService.getSchoolEntityById(userDto.getSchoolId()));
        userToUpdate.setRole(userDto.getRole());
        User updatedUser = userRepository.update(userToUpdate);
        return mapper.convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        User userToDelete = getUserEntityById(id);
        userRepository.delete(userToDelete);
    }

    private User getUserEntityById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND, id)));
    }
}
