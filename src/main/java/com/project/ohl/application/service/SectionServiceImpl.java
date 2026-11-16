package com.project.ohl.application.service;

import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.in.MediaService;
import com.project.ohl.domain.model.Section;
import java.util.List;
import java.util.stream.Stream;

public class SectionServiceImpl implements SectionService {
    private final MediaService media;

    public SectionServiceImpl(MediaService media) {
        this.media = media;
    }

    @Override
    public List<SectionDto> getAllSections() {
        return Stream.of(Section.values())
                .map(section -> new SectionDto(section, section.getTitle(),
                        media.download("images", section.getMediaKey().equals("voice_to_sign_section")
                                ? "voice/voice_to_sign_section.webp"
                                : "categories/application/" + section.getMediaKey() + ".webp").url(), section.getIconKey()))
                .toList();
    }
}
