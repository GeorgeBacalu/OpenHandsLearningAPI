package com.project.ohl.application.model;

import com.project.ohl.domain.model.Section;

public record SectionDto(Section type, String name, String mediaKey, String iconKey) {}
