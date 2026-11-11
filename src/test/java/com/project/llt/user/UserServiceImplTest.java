package com.project.llt.user;

import com.project.llt.exception.ResourceNotFoundException;
import com.project.llt.institution.InstitutionService;
import com.project.llt.role.RoleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static com.project.llt.constants.ExceptionMessageConstants.USER_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.UserMapper.convertToDto;
import static com.project.llt.mock.UserMock.getMockedUser1;
import static com.project.llt.mock.UserMock.getMockedUser2;
import static com.project.llt.mock.UserMock.getMockedUsers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private RoleService roleService;

    @Spy
    private ModelMapper modelMapper;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user1;
    private User user2;
    private List<User> users;
    private UserDto userDto1;
    private UserDto userDto2;
    private List<UserDto> userDtos;

    @BeforeEach
    void setUp() {
        user1 = getMockedUser1();
        user2 = getMockedUser2();
        users = getMockedUsers();
        userDto1 = convertToDto(modelMapper, user1);
        userDto2 = convertToDto(modelMapper, user2);
        userDtos = users.stream().map(user -> convertToDto(modelMapper, user)).toList();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        given(userDao.findAll()).willReturn(users);
        List<UserDto> result = userService.getAllUsers();
        assertThat(result).isEqualTo(userDtos);
    }

    @Test
    void getUserById_withValidId_shouldReturnUserWithGivenId() {
        given(userDao.findById(anyLong())).willReturn(Optional.ofNullable(user1));
        UserDto result = userService.getUserById(VALID_ID);
        assertThat(result).isEqualTo(userDto1);
    }

    @Test
    void getUserById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> userService.getUserById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(USER_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveUser_shouldAddUserToList() {
        given(userDao.save(any(User.class))).willReturn(user1);
        UserDto result = userService.saveUser(userDto1);
        verify(userDao).save(userCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, userCaptor.getValue()));
    }

    @Test
    void updateUserById_withValidId_shouldUpdateUserWithGivenId() {
        User user = user2; user.setId(VALID_ID);
        given(userDao.findById(anyLong())).willReturn(Optional.ofNullable(user1));
        given(userDao.update(any(User.class))).willReturn(user1);
        UserDto result = userService.updateUserById(userDto2, VALID_ID);
        verify(userDao).update(userCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, userCaptor.getValue()));
    }

    @Test
    void updateUserById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> userService.updateUserById(userDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(USER_NOT_FOUND, INVALID_ID));
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void deleteUserById_withValidId_shouldRemoveUserWithGivenIdFromList() {
        given(userDao.findById(anyLong())).willReturn(Optional.ofNullable(user1));
        userService.deleteUserById(VALID_ID);
        verify(userDao).delete(user1);
    }

    @Test
    void deleteUserById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> userService.deleteUserById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(USER_NOT_FOUND, INVALID_ID));
        verify(userDao, never()).delete(any(User.class));
    }
}
