package com.project.ohl.user;

import com.project.ohl.application.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import com.project.ohl.adapter.web.UserController;
import com.project.ohl.adapter.web.model.UserResponse;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.domain.model.User;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.UserMock.getMockedUser1;
import static com.project.ohl.mock.UserMock.getMockedUser2;
import static com.project.ohl.mock.UserMock.getMockedUsers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import java.util.UUID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private UserDto userDto1;
    private UserDto userDto2;
    private List<UserDto> userDtos;

    @BeforeEach
    void setUp() {
        userDto1 = mapper.convertToDto(getMockedUser1());
        userDto2 = mapper.convertToDto(getMockedUser2());
        userDtos = getMockedUsers().stream().map(User -> mapper.convertToDto(User)).toList();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        given(userService.getAllUsers()).willReturn(userDtos);
        ResponseEntity<List<UserResponse>> response = userController.getAllUsers();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDtos.stream().map(UserResponse::from).toList());
    }

    @Test
    void getUserById_shouldReturnUserWithGivenId() {
        given(userService.getUserById(any(UUID.class))).willReturn(userDto1);
        ResponseEntity<UserResponse> response = userController.getUserById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(UserResponse.from(userDto1));
    }

    @Test
    void saveUser_shouldAddUserToList() {
        given(userService.saveUser(any(UserDto.class))).willReturn(userDto1);
        ResponseEntity<UserResponse> response = userController.saveUser(userDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(UserResponse.from(userDto1));
    }

    @Test
    void updateUserById_shouldUpdateUserWithGivenId() {
        UserDto userDto = userDto2; userDto.setId(VALID_ID);
        given(userService.updateUserById(any(UserDto.class), any(UUID.class))).willReturn(userDto);
        ResponseEntity<UserResponse> response = userController.updateUserById(userDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(UserResponse.from(userDto));
    }

    @Test
    void deleteUserById_shouldRemoveUserWithGivenIdFromList() {
        ResponseEntity<Void> response = userController.deleteUserById(VALID_ID);
        verify(userService).deleteUserById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
