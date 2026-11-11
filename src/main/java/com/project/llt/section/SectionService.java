package com.project.llt.section;

import java.util.List;

public interface SectionService {

    List<SectionDto> getAllSections();

    SectionDto getSectionById(Long id);

    SectionDto saveSection(SectionDto sectionDto);

    SectionDto updateSectionById(SectionDto sectionDto, Long id);

    void deleteSectionById(Long id);

    Section getSectionEntityById(Long id);
}
