package com.project.llt.user;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.UserMapper.convertToDto;
import static com.project.llt.mock.UserMock.getMockedUser1;
import static com.project.llt.mock.UserMock.getMockedUser2;
import static com.project.llt.mock.UserMock.getMockedUsers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Spy
    private ModelMapper modelMapper;

    private UserDto userDto1;
    private UserDto userDto2;
    private List<UserDto> userDtos;

    @BeforeEach
    void setUp() {
        userDto1 = convertToDto(modelMapper, getMockedUser1());
        userDto2 = convertToDto(modelMapper, getMockedUser2());
        userDtos = getMockedUsers().stream().map(User -> convertToDto(modelMapper, User)).toList();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        given(userService.getAllUsers()).willReturn(userDtos);
        ResponseEntity<List<UserDto>> response = userController.getAllUsers();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDtos);
    }

    @Test
    void getUserById_shouldReturnUserWithGivenId() {
        given(userService.getUserById(anyLong())).willReturn(userDto1);
        ResponseEntity<UserDto> response = userController.getUserById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDto1);
    }

    @Test
    void saveUser_shouldAddUserToList() {
        given(userService.saveUser(any(UserDto.class))).willReturn(userDto1);
        ResponseEntity<UserDto> response = userController.saveUser(userDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(userDto1);
    }

    @Test
    void updateUserById_shouldUpdateUserWithGivenId() {
        UserDto userDto = userDto2; userDto.setId(VALID_ID);
        given(userService.updateUserById(any(UserDto.class), anyLong())).willReturn(userDto);
        ResponseEntity<UserDto> response = userController.updateUserById(userDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDto);
    }

    @Test
    void deleteUserById_shouldRemoveUserWithGivenIdFromList() {
        ResponseEntity<Void> response = userController.deleteUserById(VALID_ID);
        verify(userService).deleteUserById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
