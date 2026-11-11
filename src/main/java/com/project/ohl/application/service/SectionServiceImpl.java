package com.project.ohl.application.service;

import com.project.ohl.application.mapper.SectionMapper;
import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.domain.model.Section;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.SECTION_NOT_FOUND;

@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepositoryPort sectionRepository;

    private final SectionMapper mapper;

    @Override
    public List<SectionDto> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return sections.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public SectionDto getSectionById(Long id) {
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
    public SectionDto updateSectionById(SectionDto sectionDto, Long id) {
        Section sectionToUpdate = getSectionEntityById(id);
        sectionToUpdate.setName(sectionDto.getName());
        sectionToUpdate.setIconId(sectionDto.getIconId());
        sectionToUpdate.setImageId(sectionDto.getImageId());
        Section updatedSection = sectionRepository.update(sectionToUpdate);
        return mapper.convertToDto(updatedSection);
    }

    @Override
    public void deleteSectionById(Long id) {
        Section sectionToDelete = getSectionEntityById(id);
        sectionRepository.delete(sectionToDelete);
    }

    @Override
    public Section getSectionEntityById(Long id) {
        return sectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(SECTION_NOT_FOUND, id)));
    }
}
