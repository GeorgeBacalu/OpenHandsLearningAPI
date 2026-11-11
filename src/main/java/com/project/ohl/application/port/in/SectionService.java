package com.project.ohl.application.port.in;

import com.project.ohl.application.model.SectionDto;
import com.project.ohl.domain.model.Section;

import java.util.List;

public interface SectionService {

    List<SectionDto> getAllSections();

    SectionDto getSectionById(Long id);

    SectionDto saveSection(SectionDto sectionDto);

    SectionDto updateSectionById(SectionDto sectionDto, Long id);

    void deleteSectionById(Long id);

    Section getSectionEntityById(Long id);
}
