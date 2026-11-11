package com.project.ohl.application.port.in;

import java.util.UUID;
import com.project.ohl.application.model.SectionDto;
import com.project.ohl.domain.model.Section;

import java.util.List;

public interface SectionService {

    List<SectionDto> getAllSections();

    SectionDto getSectionById(UUID id);

    SectionDto saveSection(SectionDto sectionDto);

    SectionDto updateSectionById(SectionDto sectionDto, UUID id);

    void deleteSectionById(UUID id);

    Section getSectionEntityById(UUID id);
}
