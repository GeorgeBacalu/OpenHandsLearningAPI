package com.project.ohl.application.service;

import com.project.ohl.application.mapper.SectionMapper;
import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Section;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.SECTION_NOT_FOUND;

@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private static final List<UUID> SECTION_ORDER = List.of(
            UUID.fromString("20a6880c-2e37-32e3-8e45-07a17e99a000"),
            UUID.fromString("80008ad8-872e-3c12-96a0-209f2b699d92"),
            UUID.fromString("07329929-eb4b-3793-8b2d-f6364d711a79"),
            UUID.fromString("1deb5200-b06d-3977-9426-e2ea373195f9"));

    private final SectionRepositoryPort sectionRepository;

    private final SectionMapper mapper;

    @Override
    public List<SectionDto> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return sections.stream()
                .sorted(Comparator.comparingInt((Section section) -> sectionPosition(section.getId()))
                        .thenComparing(section -> section.getId().toString()))
                .map(mapper::convertToDto).toList();
    }

    @Override
    public SectionDto getSectionById(UUID id) {
        Section section = getSectionEntityById(id);
        return mapper.convertToDto(section);
    }

    @Override
    public SectionDto saveSection(SectionDto sectionDto) {
        Section section = mapper.convertToEntity(sectionDto);
        Section savedSection = sectionRepository.save(section);
        return mapper.convertToDto(savedSection);
    }

    @Override
    public SectionDto updateSectionById(SectionDto sectionDto, UUID id) {
        Section sectionToUpdate = getSectionEntityById(id);
        sectionToUpdate.setMediaKey(sectionDto.getMediaKey());
        sectionToUpdate.setIconKey(sectionDto.getIconKey());
        sectionToUpdate.setName(sectionDto.getName());

        Section updatedSection = sectionRepository.update(sectionToUpdate);
        return mapper.convertToDto(updatedSection);
    }

    @Override
    public void deleteSectionById(UUID id) {
        Section sectionToDelete = getSectionEntityById(id);
        sectionRepository.delete(sectionToDelete);
    }

    @Override
    public Section getSectionEntityById(UUID id) {
        return sectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(SECTION_NOT_FOUND, id)));
    }
    private static int sectionPosition(UUID id) {
        int position = SECTION_ORDER.indexOf(id);
        return position < 0 ? SECTION_ORDER.size() : position;
    }

}
