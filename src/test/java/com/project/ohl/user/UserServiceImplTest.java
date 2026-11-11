package com.project.ohl.user;

import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.application.service.UserServiceImpl;
import com.project.ohl.domain.model.User;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.in.RoleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.project.ohl.application.error.ExceptionMessageConstants.USER_NOT_FOUND;
import static com.project.ohl.constants.IdentifierConstants.INVALID_ID;
import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.application.mapper.UserMapper.convertToDto;
import static com.project.ohl.mock.UserMock.getMockedUser1;
import static com.project.ohl.mock.UserMock.getMockedUser2;
import static com.project.ohl.mock.UserMock.getMockedUsers;
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
    private UserRepositoryPort userRepository;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private RoleService roleService;

    @Mock
    private com.project.ohl.application.port.out.PasswordHasher passwordHasher;

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
        userDto1 = convertToDto(user1);
        userDto2 = convertToDto(user2);
        userDtos = users.stream().map(user -> convertToDto(user)).toList();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        given(userRepository.findAll()).willReturn(users);
        List<UserDto> result = userService.getAllUsers();
        assertThat(result).isEqualTo(userDtos);
    }

    @Test
    void getUserById_withValidId_shouldReturnUserWithGivenId() {
        given(userRepository.findById(anyLong())).willReturn(Optional.ofNullable(user1));
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
        given(passwordHasher.hash(userDto1.getPassword())).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        UserDto result = userService.saveUser(userDto1);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(result).isEqualTo(convertToDto(userCaptor.getValue()));
    }

    @Test
    void updateUserById_withValidId_shouldUpdateUserWithGivenId() {
        User user = user2; user.setId(VALID_ID);
        given(userRepository.findById(anyLong())).willReturn(Optional.ofNullable(user1));
        given(passwordHasher.hash(userDto2.getPassword())).willReturn("encoded-password");
        given(userRepository.update(any(User.class))).willReturn(user1);
        UserDto result = userService.updateUserById(userDto2, VALID_ID);
        verify(userRepository).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(result).isEqualTo(convertToDto(userCaptor.getValue()));
    }

    @Test
    void updateUserById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> userService.updateUserById(userDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(USER_NOT_FOUND, INVALID_ID));
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    void deleteUserById_withValidId_shouldRemoveUserWithGivenIdFromList() {
        given(userRepository.findById(anyLong())).willReturn(Optional.ofNullable(user1));
        userService.deleteUserById(VALID_ID);
        verify(userRepository).delete(user1);
    }

    @Test
    void deleteUserById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> userService.deleteUserById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(USER_NOT_FOUND, INVALID_ID));
        verify(userRepository, never()).delete(any(User.class));
    }
}
