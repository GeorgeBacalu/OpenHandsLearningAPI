package com.project.ohl.school;

import com.project.ohl.adapter.web.SchoolController;
import com.project.ohl.application.mapper.SchoolMapper;
import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.application.port.in.SchoolService;
import com.project.ohl.domain.model.School;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.SchoolMock.getMockedSchool1;
import static com.project.ohl.mock.SchoolMock.getMockedSchool2;
import static com.project.ohl.mock.SchoolMock.getMockedSchools;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchoolControllerTest {

    private final SchoolMapper mapper = Mappers.getMapper(SchoolMapper.class);

    @InjectMocks
    private SchoolController schoolController;

    @Mock
    private SchoolService schoolService;

    private SchoolDto schoolDto1;
    private SchoolDto schoolDto2;
    private List<SchoolDto> schoolDtos;

    @BeforeEach
    void setUp() {
        schoolDto1 = mapper.convertToDto(getMockedSchool1());
        schoolDto2 = mapper.convertToDto(getMockedSchool2());
        schoolDtos = getMockedSchools().stream().map(School -> mapper.convertToDto(School)).toList();
    }

    @Test
    void getAllSchools_shouldReturnListOfSchools() {
        given(schoolService.getAllSchools()).willReturn(schoolDtos);
        ResponseEntity<List<SchoolDto>> response = schoolController.getAllSchools();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(schoolDtos);
    }

    @Test
    void getSchoolById_shouldReturnSchoolWithGivenId() {
        given(schoolService.getSchoolById(any(UUID.class))).willReturn(schoolDto1);
        ResponseEntity<SchoolDto> response = schoolController.getSchoolById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(schoolDto1);
    }

    @Test
    void saveSchool_shouldAddSchoolToList() {
        given(schoolService.saveSchool(any(SchoolDto.class))).willReturn(schoolDto1);
        ResponseEntity<SchoolDto> response = schoolController.saveSchool(schoolDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(schoolDto1);
    }

    @Test
    void updateSchoolById_shouldUpdateSchoolWithGivenId() {
        SchoolDto schoolDto = schoolDto2; schoolDto.setId(VALID_ID);
        given(schoolService.updateSchoolById(any(SchoolDto.class), any(UUID.class))).willReturn(schoolDto);
        ResponseEntity<SchoolDto> response = schoolController.updateSchoolById(schoolDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(schoolDto);
    }

    @Test
    void deleteSchoolById_shouldRemoveSchoolWithGivenIdFromList() {
        ResponseEntity<Void> response = schoolController.deleteSchoolById(VALID_ID);
        verify(schoolService).deleteSchoolById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
