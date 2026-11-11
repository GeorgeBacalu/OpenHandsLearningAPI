package com.project.ohl.role;

import com.project.ohl.application.mapper.RoleMapper;
import com.project.ohl.application.model.RoleDto;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.service.RoleServiceImpl;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.project.ohl.application.error.ExceptionMessageConstants.ROLE_NOT_FOUND;
import static com.project.ohl.constants.IdentifierConstants.INVALID_ID;
import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.RoleMock.getMockedRole1;
import static com.project.ohl.mock.RoleMock.getMockedRole2;
import static com.project.ohl.mock.RoleMock.getMockedRoles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Spy
    private final RoleMapper mapper = Mappers.getMapper(RoleMapper.class);

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private RoleRepositoryPort roleRepository;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    private Role role1;
    private Role role2;
    private List<Role> roles;
    private RoleDto roleDto1;
    private RoleDto roleDto2;
    private List<RoleDto> roleDtos;

    @BeforeEach
    void setUp() {
        role1 = getMockedRole1();
        role2 = getMockedRole2();
        roles = getMockedRoles();
        roleDto1 = mapper.convertToDto(role1);
        roleDto2 = mapper.convertToDto(role2);
        roleDtos = roles.stream().map(role -> mapper.convertToDto(role)).toList();
    }

    @Test
    void getAllRoles_shouldReturnListOfRoles() {
        given(roleRepository.findAll()).willReturn(roles);
        List<RoleDto> result = roleService.getAllRoles();
        assertThat(result).isEqualTo(roleDtos);
    }

    @Test
    void getRoleById_withValidId_shouldReturnRoleWithGivenId() {
        given(roleRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(role1));
        RoleDto result = roleService.getRoleById(VALID_ID);
        assertThat(result).isEqualTo(roleDto1);
    }

    @Test
    void getRoleById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> roleService.getRoleById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(ROLE_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveRole_shouldAddRoleToList() {
        given(roleRepository.save(any(Role.class))).willReturn(role1);
        RoleDto result = roleService.saveRole(roleDto1);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(roleCaptor.getValue()));
    }

    @Test
    void updateRoleById_withValidId_shouldUpdateRoleWithGivenId() {
        Role role = role2; role.setId(VALID_ID);
        given(roleRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(role1));
        given(roleRepository.update(any(Role.class))).willReturn(role);
        RoleDto result = roleService.updateRoleById(roleDto2, VALID_ID);
        verify(roleRepository).update(roleCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(roleCaptor.getValue()));
    }

    @Test
    void updateRoleById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> roleService.updateRoleById(roleDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(ROLE_NOT_FOUND, INVALID_ID));
        verify(roleRepository, never()).update(any(Role.class));
    }

    @Test
    void deleteRoleById_withValidId_shouldRemoveRoleWithGivenIdFromList() {
        given(roleRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(role1));
        roleService.deleteRoleById(VALID_ID);
        verify(roleRepository).delete(role1);
    }

    @Test
    void deleteRoleById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> roleService.deleteRoleById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(ROLE_NOT_FOUND, INVALID_ID));
        verify(roleRepository, never()).delete(any(Role.class));
    }
}
