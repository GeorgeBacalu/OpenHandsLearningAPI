package com.project.llt.user;

import com.project.llt.exception.ResourceNotFoundException;
import com.project.llt.institution.InstitutionService;
import com.project.llt.role.RoleService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.project.llt.constants.ExceptionMessageConstants.USER_NOT_FOUND;
import static com.project.llt.mapper.UserMapper.convertToDto;
import static com.project.llt.mapper.UserMapper.convertToEntity;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final InstitutionService institutionService;
    private final RoleService roleService;
    private final ModelMapper modelMapper;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userDao.findAll();
        return !users.isEmpty() ? users.stream().map(user -> convertToDto(modelMapper, user)).toList() : new ArrayList<>();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = getUserEntityById(id);
        return convertToDto(modelMapper, user);
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = convertToEntity(modelMapper, userDto);
        User savedUser = userDao.save(user);
        return convertToDto(modelMapper, savedUser);
    }

    @Override
    public UserDto updateUserById(UserDto userDto, Long id) {
        User userToUpdate = getUserEntityById(id);
        userToUpdate.setName(userDto.getName());
        userToUpdate.setEmail(userDto.getEmail());
        userToUpdate.setPassword(userDto.getPassword());
        userToUpdate.setMobile(userDto.getMobile());
        userToUpdate.setAddress(userDto.getAddress());
        userToUpdate.setBirthday(userDto.getBirthday());
        userToUpdate.setInstitution(institutionService.getInstitutionEntityById(userDto.getInstitutionId()));
        userToUpdate.setRole(roleService.getRoleEntityById(userDto.getRoleId()));
        User updatedUser = userDao.update(userToUpdate);
        return convertToDto(modelMapper, updatedUser);
    }

    @Override
    public void deleteUserById(Long id) {
        User userToDelete = getUserEntityById(id);
        userDao.delete(userToDelete);
    }

    @Override
    public User getUserEntityById(Long id) {
        return userDao.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND, id)));
    }
}
