package com.project.llt.institution;

import com.project.llt.exception.ResourceNotFoundException;
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

import static com.project.llt.constants.ExceptionMessageConstants.INSTITUTION_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.InstitutionMapper.convertToDto;
import static com.project.llt.mock.InstitutionMock.getMockedInstitution1;
import static com.project.llt.mock.InstitutionMock.getMockedInstitution2;
import static com.project.llt.mock.InstitutionMock.getMockedInstitutions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {
    
    @InjectMocks
    private InstitutionServiceImpl institutionService;
    
    @Mock
    private InstitutionDao institutionDao;
    
    @Spy
    private ModelMapper modelMapper;
    
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
        institutionDto1 = convertToDto(modelMapper, institution1);
        institutionDto2 = convertToDto(modelMapper, institution2);
        institutionDtos = institutions.stream().map(institution -> convertToDto(modelMapper, institution)).toList();
    }

    @Test
    void getAllInstitutions_shouldReturnListOfInstitutions() {
        given(institutionDao.findAll()).willReturn(institutions);
        List<InstitutionDto> result = institutionService.getAllInstitutions();
        assertThat(result).isEqualTo(institutionDtos);
    }

    @Test
    void getInstitutionById_withValidId_shouldReturnInstitutionWithGivenId() {
        given(institutionDao.findById(anyLong())).willReturn(Optional.ofNullable(institution1));
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
        given(institutionDao.save(any(Institution.class))).willReturn(institution1);
        InstitutionDto result = institutionService.saveInstitution(institutionDto1);
        verify(institutionDao).save(institutionCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, institutionCaptor.getValue()));
    }

    @Test
    void updateInstitutionById_withValidId_shouldUpdateInstitutionWithGivenId() {
        Institution institution = institution2; institution.setId(VALID_ID);
        given(institutionDao.findById(anyLong())).willReturn(Optional.ofNullable(institution1));
        given(institutionDao.update(any(Institution.class))).willReturn(institution);
        InstitutionDto result = institutionService.updateInstitutionById(institutionDto2, VALID_ID);
        verify(institutionDao).update(institutionCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, institutionCaptor.getValue()));
    }

    @Test
    void updateInstitutionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> institutionService.updateInstitutionById(institutionDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(INSTITUTION_NOT_FOUND, INVALID_ID));
        verify(institutionDao, never()).update(any(Institution.class));
    }

    @Test
    void deleteInstitutionById_withValidId_shouldRemoveInstitutionWithGivenIdFromList() {
        given(institutionDao.findById(anyLong())).willReturn(Optional.ofNullable(institution1));
        institutionService.deleteInstitutionById(VALID_ID);
        verify(institutionDao).delete(institution1);
    }

    @Test
    void deleteInstitutionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> institutionService.deleteInstitutionById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(INSTITUTION_NOT_FOUND, INVALID_ID));
        verify(institutionDao, never()).delete(any(Institution.class));
    }
}
