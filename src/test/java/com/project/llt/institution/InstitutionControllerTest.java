package com.project.llt.institution;

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
import static com.project.llt.mapper.InstitutionMapper.convertToDto;
import static com.project.llt.mock.InstitutionMock.getMockedInstitution1;
import static com.project.llt.mock.InstitutionMock.getMockedInstitution2;
import static com.project.llt.mock.InstitutionMock.getMockedInstitutions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstitutionControllerTest {

    @InjectMocks
    private InstitutionController institutionController;

    @Mock
    private InstitutionService institutionService;

    @Spy
    private ModelMapper modelMapper;

    private InstitutionDto institutionDto1;
    private InstitutionDto institutionDto2;
    private List<InstitutionDto> institutionDtos;

    @BeforeEach
    void setUp() {
        institutionDto1 = convertToDto(modelMapper, getMockedInstitution1());
        institutionDto2 = convertToDto(modelMapper, getMockedInstitution2());
        institutionDtos = getMockedInstitutions().stream().map(Institution -> convertToDto(modelMapper, Institution)).toList();
    }

    @Test
    void getAllInstitutions_shouldReturnListOfInstitutions() {
        given(institutionService.getAllInstitutions()).willReturn(institutionDtos);
        ResponseEntity<List<InstitutionDto>> response = institutionController.getAllInstitutions();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(institutionDtos);
    }

    @Test
    void getInstitutionById_shouldReturnInstitutionWithGivenId() {
        given(institutionService.getInstitutionById(anyLong())).willReturn(institutionDto1);
        ResponseEntity<InstitutionDto> response = institutionController.getInstitutionById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(institutionDto1);
    }

    @Test
    void saveInstitution_shouldAddInstitutionToList() {
        given(institutionService.saveInstitution(any(InstitutionDto.class))).willReturn(institutionDto1);
        ResponseEntity<InstitutionDto> response = institutionController.saveInstitution(institutionDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(institutionDto1);
    }

    @Test
    void updateInstitutionById_shouldUpdateInstitutionWithGivenId() {
        InstitutionDto institutionDto = institutionDto2; institutionDto.setId(VALID_ID);
        given(institutionService.updateInstitutionById(any(InstitutionDto.class), anyLong())).willReturn(institutionDto);
        ResponseEntity<InstitutionDto> response = institutionController.updateInstitutionById(institutionDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(institutionDto);
    }

    @Test
    void deleteInstitutionById_shouldRemoveInstitutionWithGivenIdFromList() {
        ResponseEntity<Void> response = institutionController.deleteInstitutionById(VALID_ID);
        verify(institutionService).deleteInstitutionById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
