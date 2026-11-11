package com.project.llt.section;

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
import static com.project.llt.mapper.SectionMapper.convertToDto;
import static com.project.llt.mock.SectionMock.getMockedSection1;
import static com.project.llt.mock.SectionMock.getMockedSection2;
import static com.project.llt.mock.SectionMock.getMockedSections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SectionControllerTest {

    @InjectMocks
    private SectionController sectionController;

    @Mock
    private SectionService sectionService;

    @Spy
    private ModelMapper modelMapper;

    private SectionDto sectionDto1;
    private SectionDto sectionDto2;
    private List<SectionDto> sectionDtos;

    @BeforeEach
    void setUp() {
        sectionDto1 = convertToDto(modelMapper, getMockedSection1());
        sectionDto2 = convertToDto(modelMapper, getMockedSection2());
        sectionDtos = getMockedSections().stream().map(Section -> convertToDto(modelMapper, Section)).toList();
    }

    @Test
    void getAllSections_shouldReturnListOfSections() {
        given(sectionService.getAllSections()).willReturn(sectionDtos);
        ResponseEntity<List<SectionDto>> response = sectionController.getAllSections();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sectionDtos);
    }

    @Test
    void getSectionById_shouldReturnSectionWithGivenId() {
        given(sectionService.getSectionById(anyLong())).willReturn(sectionDto1);
        ResponseEntity<SectionDto> response = sectionController.getSectionById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sectionDto1);
    }

    @Test
    void saveSection_shouldAddSectionToList() {
        given(sectionService.saveSection(any(SectionDto.class))).willReturn(sectionDto1);
        ResponseEntity<SectionDto> response = sectionController.saveSection(sectionDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sectionDto1);
    }

    @Test
    void updateSectionById_shouldUpdateSectionWithGivenId() {
        SectionDto sectionDto = sectionDto2; sectionDto.setId(VALID_ID);
        given(sectionService.updateSectionById(any(SectionDto.class), anyLong())).willReturn(sectionDto);
        ResponseEntity<SectionDto> response = sectionController.updateSectionById(sectionDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sectionDto);
    }

    @Test
    void deleteSectionById_shouldRemoveSectionWithGivenIdFromList() {
        ResponseEntity<Void> response = sectionController.deleteSectionById(VALID_ID);
        verify(sectionService).deleteSectionById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
