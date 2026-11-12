package com.project.ohl.application.service;

import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.domain.model.Section;
import java.util.List;
import java.util.stream.Stream;

public class SectionServiceImpl implements SectionService {
    @Override
    public List<SectionDto> getAllSections() {
        return Stream.of(Section.values())
                .map(section -> new SectionDto(section, section.getTitle(), section.getMediaKey(), section.getIconKey()))
                .toList();
    }
}
