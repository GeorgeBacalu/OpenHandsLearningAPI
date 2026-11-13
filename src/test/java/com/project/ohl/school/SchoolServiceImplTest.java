package com.project.ohl.school;

import com.project.ohl.application.mapper.SchoolMapper;
import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.application.service.SchoolServiceImpl;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.School;
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

import static com.project.ohl.application.error.ExceptionMessageConstants.SCHOOL_NOT_FOUND;
import static com.project.ohl.constants.IdentifierConstants.INVALID_ID;
import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.SchoolMock.getMockedSchool1;
import static com.project.ohl.mock.SchoolMock.getMockedSchool2;
import static com.project.ohl.mock.SchoolMock.getMockedSchools;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {

    @Spy
    private final SchoolMapper mapper = Mappers.getMapper(SchoolMapper.class);

    @InjectMocks
    private SchoolServiceImpl schoolService;

    @Mock
    private SchoolRepositoryPort schoolRepository;


    @Captor
    private ArgumentCaptor<School> schoolCaptor;

    private School school1;
    private School school2;
    private List<School> schools;
    private SchoolDto schoolDto1;
    private SchoolDto schoolDto2;
    private List<SchoolDto> schoolDtos;

    @BeforeEach
    void setUp() {
        school1 = getMockedSchool1();
        school2 = getMockedSchool2();
        schools = getMockedSchools();
        schoolDto1 = mapper.convertToDto(school1);
        schoolDto2 = mapper.convertToDto(school2);
        schoolDtos = schools.stream().map(school -> mapper.convertToDto(school)).toList();
    }

    @Test
    void getAllSchools_shouldReturnListOfSchools() {
        given(schoolRepository.findAll()).willReturn(schools);
        List<SchoolDto> result = schoolService.getAllSchools();
        assertThat(result).isEqualTo(schoolDtos);
    }

    @Test
    void getSchoolById_withValidId_shouldReturnSchoolWithGivenId() {
        given(schoolRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(school1));
        SchoolDto result = schoolService.getSchoolById(VALID_ID);
        assertThat(result).isEqualTo(schoolDto1);
    }

    @Test
    void getSchoolById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> schoolService.getSchoolById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SCHOOL_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveSchool_shouldAddSchoolToList() {
        given(schoolRepository.save(any(School.class))).willReturn(school1);
        SchoolDto result = schoolService.saveSchool(schoolDto1);
        verify(schoolRepository).save(schoolCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(schoolCaptor.getValue()));
    }

    @Test
    void updateSchoolById_withValidId_shouldUpdateSchoolWithGivenId() {
        School school = school2; school.setId(VALID_ID);
        given(schoolRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(school1));
        given(schoolRepository.update(any(School.class))).willReturn(school);
        SchoolDto result = schoolService.updateSchoolById(schoolDto2, VALID_ID);
        verify(schoolRepository).update(schoolCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(schoolCaptor.getValue()));
    }

    @Test
    void updateSchoolById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> schoolService.updateSchoolById(schoolDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SCHOOL_NOT_FOUND, INVALID_ID));
        verify(schoolRepository, never()).update(any(School.class));
    }

    @Test
    void deleteSchoolById_withValidId_shouldRemoveSchoolWithGivenIdFromList() {
        given(schoolRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(school1));
        schoolService.deleteSchoolById(VALID_ID);
        verify(schoolRepository).delete(school1);
    }

    @Test
    void deleteSchoolById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> schoolService.deleteSchoolById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SCHOOL_NOT_FOUND, INVALID_ID));
        verify(schoolRepository, never()).delete(any(School.class));
    }
}
