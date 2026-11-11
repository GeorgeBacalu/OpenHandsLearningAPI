package com.project.ohl.institution;

import com.project.ohl.application.mapper.InstitutionMapper;
import org.mapstruct.factory.Mappers;
import com.project.ohl.application.model.InstitutionDto;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.application.service.InstitutionServiceImpl;
import com.project.ohl.domain.model.Institution;

import com.project.ohl.domain.exception.ResourceNotFoundException;
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

import static com.project.ohl.application.error.ExceptionMessageConstants.INSTITUTION_NOT_FOUND;
import static com.project.ohl.constants.IdentifierConstants.INVALID_ID;
import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.InstitutionMock.getMockedInstitution1;
import static com.project.ohl.mock.InstitutionMock.getMockedInstitution2;
import static com.project.ohl.mock.InstitutionMock.getMockedInstitutions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import java.util.UUID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @org.mockito.Spy
    private final InstitutionMapper mapper = Mappers.getMapper(InstitutionMapper.class);
    
    @InjectMocks
    private InstitutionServiceImpl institutionService;
    
    @Mock
    private InstitutionRepositoryPort institutionRepository;
    
    
    @Captor
    private ArgumentCaptor<Institution> institutionCaptor;
    
    private Institution institution1;
    private Institution institution2;
    private List<Institution> institutions;
    private InstitutionDto institutionDto1;
    private InstitutionDto institutionDto2;
    private List<InstitutionDto> institutionDtos;

    @BeforeEach
    void setUp() {
        institution1 = getMockedInstitution1();
        institution2 = getMockedInstitution2();
        institutions = getMockedInstitutions();
        institutionDto1 = mapper.convertToDto(institution1);
        institutionDto2 = mapper.convertToDto(institution2);
        institutionDtos = institutions.stream().map(institution -> mapper.convertToDto(institution)).toList();
    }

    @Test
    void getAllInstitutions_shouldReturnListOfInstitutions() {
        given(institutionRepository.findAll()).willReturn(institutions);
        List<InstitutionDto> result = institutionService.getAllInstitutions();
        assertThat(result).isEqualTo(institutionDtos);
    }

    @Test
    void getInstitutionById_withValidId_shouldReturnInstitutionWithGivenId() {
        given(institutionRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(institution1));
        InstitutionDto result = institutionService.getInstitutionById(VALID_ID);
        assertThat(result).isEqualTo(institutionDto1);
    }

    @Test
    void getInstitutionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> institutionService.getInstitutionById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(INSTITUTION_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveInstitution_shouldAddInstitutionToList() {
        given(institutionRepository.save(any(Institution.class))).willReturn(institution1);
        InstitutionDto result = institutionService.saveInstitution(institutionDto1);
        verify(institutionRepository).save(institutionCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(institutionCaptor.getValue()));
    }

    @Test
    void updateInstitutionById_withValidId_shouldUpdateInstitutionWithGivenId() {
        Institution institution = institution2; institution.setId(VALID_ID);
        given(institutionRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(institution1));
        given(institutionRepository.update(any(Institution.class))).willReturn(institution);
        InstitutionDto result = institutionService.updateInstitutionById(institutionDto2, VALID_ID);
        verify(institutionRepository).update(institutionCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(institutionCaptor.getValue()));
    }

    @Test
    void updateInstitutionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> institutionService.updateInstitutionById(institutionDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(INSTITUTION_NOT_FOUND, INVALID_ID));
        verify(institutionRepository, never()).update(any(Institution.class));
    }

    @Test
    void deleteInstitutionById_withValidId_shouldRemoveInstitutionWithGivenIdFromList() {
        given(institutionRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(institution1));
        institutionService.deleteInstitutionById(VALID_ID);
        verify(institutionRepository).delete(institution1);
    }

    @Test
    void deleteInstitutionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> institutionService.deleteInstitutionById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(INSTITUTION_NOT_FOUND, INVALID_ID));
        verify(institutionRepository, never()).delete(any(Institution.class));
    }
}
