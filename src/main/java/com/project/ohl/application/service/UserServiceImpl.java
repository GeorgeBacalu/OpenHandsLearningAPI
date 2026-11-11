package com.project.ohl.application.service;

import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.in.RoleService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.ClassroomRepositoryPort;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.USER_NOT_FOUND;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepository;
    private final InstitutionService institutionService;
    private final RoleService roleService;
    private final PasswordHasher passwordHasher;

    private final UserMapper mapper;
    private final ClassroomRepositoryPort classrooms;

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
    public UserDto saveUser(UserDto userDto) {
        User user = mapper.convertToEntity(userDto);
        validateClassroom(userDto);
        user.setPassword(passwordHasher.hash(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return mapper.convertToDto(savedUser);
    }

    @Override
    public UserDto updateUserById(UserDto userDto, UUID id) {
        User userToUpdate = getUserEntityById(id);
        validateClassroom(userDto);
        userToUpdate.setClassroomId(userDto.getClassroomId());
        userToUpdate.setName(userDto.getName());
        userToUpdate.setEmail(userDto.getEmail());
        userToUpdate.setPassword(passwordHasher.hash(userDto.getPassword()));
        userToUpdate.setMobile(userDto.getMobile());
        userToUpdate.setAddress(userDto.getAddress());
        userToUpdate.setBirthday(userDto.getBirthday());
        userToUpdate.setInstitution(institutionService.getInstitutionEntityById(userDto.getInstitutionId()));
        userToUpdate.setRole(roleService.getRoleEntityById(userDto.getRoleId()));
        User updatedUser = userRepository.update(userToUpdate);
        return mapper.convertToDto(updatedUser);
    }

    @Override
    public void deleteUserById(UUID id) {
        User userToDelete = getUserEntityById(id);
        userRepository.delete(userToDelete);
    }

    private void validateClassroom(UserDto dto) {
        if (dto.getClassroomId() == null) return;
        var classroom = classrooms.findById(dto.getClassroomId()).orElseThrow(() -> new ResourceNotFoundException("Classroom was not found"));
        if (!classroom.getInstitutionId().equals(dto.getInstitutionId()))
            throw new InvalidRequestException("Classroom must belong to the selected institution.");
    }

    @Override
    public User getUserEntityById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND, id)));
    }
}
