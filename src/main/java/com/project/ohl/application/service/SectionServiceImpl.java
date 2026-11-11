package com.project.ohl.application.service;

import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.domain.model.Section;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.SECTION_NOT_FOUND;
import static com.project.ohl.application.mapper.SectionMapper.convertToDto;
import static com.project.ohl.application.mapper.SectionMapper.convertToEntity;

@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepositoryPort sectionRepository;

    @Override
    public List<SectionDto> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return sections.stream().map(section -> convertToDto(section)).toList();
    }

    @Override
    public SectionDto getSectionById(Long id) {
        Section section = getSectionEntityById(id);
        return convertToDto(section);
    }

    @Override
    public SectionDto saveSection(SectionDto sectionDto) {
        Section section = convertToEntity(sectionDto);
        Section savedSection = sectionRepository.save(section);
        return convertToDto(savedSection);
    }

    @Override
    public SectionDto updateSectionById(SectionDto sectionDto, Long id) {
        Section sectionToUpdate = getSectionEntityById(id);
        sectionToUpdate.setName(sectionDto.getName());
        sectionToUpdate.setIconId(sectionDto.getIconId());
        sectionToUpdate.setImageId(sectionDto.getImageId());
        Section updatedSection = sectionRepository.update(sectionToUpdate);
        return convertToDto(updatedSection);
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
