package com.project.llt.section;

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

import static com.project.llt.constants.ExceptionMessageConstants.SECTION_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.SectionMapper.convertToDto;
import static com.project.llt.mock.SectionMock.getMockedSection1;
import static com.project.llt.mock.SectionMock.getMockedSection2;
import static com.project.llt.mock.SectionMock.getMockedSections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @InjectMocks
    private SectionServiceImpl sectionService;

    @Mock
    private SectionDao sectionDao;

    @Spy
    private ModelMapper modelMapper;

    @Captor
    private ArgumentCaptor<Section> sectionCaptor;

    private Section section1;
    private Section section2;
    private List<Section> sections;
    private SectionDto sectionDto1;
    private SectionDto sectionDto2;
    private List<SectionDto> sectionDtos;

    @BeforeEach
    void setUp() {
        section1 = getMockedSection1();
        section2 = getMockedSection2();
        sections = getMockedSections();
        sectionDto1 = convertToDto(modelMapper, section1);
        sectionDto2 = convertToDto(modelMapper, section2);
        sectionDtos = sections.stream().map(section -> convertToDto(modelMapper, section)).toList();
    }

    @Test
    void getAllSections_shouldReturnListOfSections() {
        given(sectionDao.findAll()).willReturn(sections);
        List<SectionDto> result = sectionService.getAllSections();
        assertThat(result).isEqualTo(sectionDtos);
    }

    @Test
    void getSectionById_withValidId_shouldReturnSectionWithGivenId() {
        given(sectionDao.findById(anyLong())).willReturn(Optional.ofNullable(section1));
        SectionDto result = sectionService.getSectionById(VALID_ID);
        assertThat(result).isEqualTo(sectionDto1);
    }

    @Test
    void getSectionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> sectionService.getSectionById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SECTION_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveSection_shouldAddSectionToList() {
        given(sectionDao.save(any(Section.class))).willReturn(section1);
        SectionDto result = sectionService.saveSection(sectionDto1);
        verify(sectionDao).save(sectionCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, sectionCaptor.getValue()));
    }

    @Test
    void updateSectionById_withValidId_shouldUpdateSectionWithGivenId() {
        Section section = section2; section.setId(VALID_ID);
        given(sectionDao.findById(anyLong())).willReturn(Optional.ofNullable(section1));
        given(sectionDao.update(any(Section.class))).willReturn(section);
        SectionDto result = sectionService.updateSectionById(sectionDto2, VALID_ID);
        verify(sectionDao).update(sectionCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, sectionCaptor.getValue()));
    }

    @Test
    void updateSectionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> sectionService.updateSectionById(sectionDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SECTION_NOT_FOUND, INVALID_ID));
        verify(sectionDao, never()).update(any(Section.class));
    }

    @Test
    void deleteSectionById_withValidId_shouldRemoveSectionWithGivenIdFromList() {
        given(sectionDao.findById(anyLong())).willReturn(Optional.ofNullable(section1));
        sectionService.deleteSectionById(VALID_ID);
        verify(sectionDao).delete(section1);
    }

    @Test
    void deleteSectionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> sectionService.deleteSectionById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SECTION_NOT_FOUND, INVALID_ID));
        verify(sectionDao, never()).delete(any(Section.class));
    }
}
