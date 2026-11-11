package com.project.llt.role;

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
import static com.project.llt.mapper.RoleMapper.convertToDto;
import static com.project.llt.mock.RoleMock.getMockedRole1;
import static com.project.llt.mock.RoleMock.getMockedRole2;
import static com.project.llt.mock.RoleMock.getMockedRoles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @InjectMocks
    private RoleController roleController;

    @Mock
    private RoleService roleService;

    @Spy
    private ModelMapper modelMapper;

    private RoleDto roleDto1;
    private RoleDto roleDto2;
    private List<RoleDto> roleDtos;

    @BeforeEach
    void setUp() {
        roleDto1 = convertToDto(modelMapper, getMockedRole1());
        roleDto2 = convertToDto(modelMapper, getMockedRole2());
        roleDtos = getMockedRoles().stream().map(Role -> convertToDto(modelMapper, Role)).toList();
    }

    @Test
    void getAllRoles_shouldReturnListOfRoles() {
        given(roleService.getAllRoles()).willReturn(roleDtos);
        ResponseEntity<List<RoleDto>> response = roleController.getAllRoles();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roleDtos);
    }

    @Test
    void getRoleById_shouldReturnRoleWithGivenId() {
        given(roleService.getRoleById(anyLong())).willReturn(roleDto1);
        ResponseEntity<RoleDto> response = roleController.getRoleById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roleDto1);
    }

    @Test
    void saveRole_shouldAddRoleToList() {
        given(roleService.saveRole(any(RoleDto.class))).willReturn(roleDto1);
        ResponseEntity<RoleDto> response = roleController.saveRole(roleDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(roleDto1);
    }

    @Test
    void updateRoleById_shouldUpdateRoleWithGivenId() {
        RoleDto roleDto = roleDto2; roleDto.setId(VALID_ID);
        given(roleService.updateRoleById(any(RoleDto.class), anyLong())).willReturn(roleDto);
        ResponseEntity<RoleDto> response = roleController.updateRoleById(roleDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roleDto);
    }

    @Test
    void deleteRoleById_shouldRemoveRoleWithGivenIdFromList() {
        ResponseEntity<Void> response = roleController.deleteRoleById(VALID_ID);
        verify(roleService).deleteRoleById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
