package com.project.ohl.section;

import com.project.ohl.application.mapper.SectionMapper;
import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.application.service.SectionServiceImpl;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Section;
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

import static com.project.ohl.application.error.ExceptionMessageConstants.SECTION_NOT_FOUND;
import static com.project.ohl.constants.IdentifierConstants.INVALID_ID;
import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.SectionMock.getMockedSection1;
import static com.project.ohl.mock.SectionMock.getMockedSection2;
import static com.project.ohl.mock.SectionMock.getMockedSections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @Spy
    private final SectionMapper mapper = Mappers.getMapper(SectionMapper.class);

    @InjectMocks
    private SectionServiceImpl sectionService;

    @Mock
    private SectionRepositoryPort sectionRepository;

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
        sectionDto1 = mapper.convertToDto(section1);
        sectionDto2 = mapper.convertToDto(section2);
        sectionDtos = sections.stream().map(section -> mapper.convertToDto(section)).toList();
    }

    @Test
    void getAllSections_shouldReturnListOfSections() {
        given(sectionRepository.findAll()).willReturn(sections);
        List<SectionDto> result = sectionService.getAllSections();
        assertThat(result).isEqualTo(sectionDtos);
    }

    @Test
    void getSectionById_withValidId_shouldReturnSectionWithGivenId() {
        given(sectionRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(section1));
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
        given(sectionRepository.save(any(Section.class))).willReturn(section1);
        SectionDto result = sectionService.saveSection(sectionDto1);
        verify(sectionRepository).save(sectionCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(sectionCaptor.getValue()));
    }

    @Test
    void updateSectionById_withValidId_shouldUpdateSectionWithGivenId() {
        Section section = section2; section.setId(VALID_ID);
        given(sectionRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(section1));
        given(sectionRepository.update(any(Section.class))).willReturn(section);
        SectionDto result = sectionService.updateSectionById(sectionDto2, VALID_ID);
        verify(sectionRepository).update(sectionCaptor.capture());
        assertThat(result).isEqualTo(mapper.convertToDto(sectionCaptor.getValue()));
    }

    @Test
    void updateSectionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> sectionService.updateSectionById(sectionDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SECTION_NOT_FOUND, INVALID_ID));
        verify(sectionRepository, never()).update(any(Section.class));
    }

    @Test
    void deleteSectionById_withValidId_shouldRemoveSectionWithGivenIdFromList() {
        given(sectionRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(section1));
        sectionService.deleteSectionById(VALID_ID);
        verify(sectionRepository).delete(section1);
    }

    @Test
    void deleteSectionById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> sectionService.deleteSectionById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(SECTION_NOT_FOUND, INVALID_ID));
        verify(sectionRepository, never()).delete(any(Section.class));
    }
}
