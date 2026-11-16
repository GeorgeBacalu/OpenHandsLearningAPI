package com.project.ohl.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Section {
    VIDEO("Secțiune video", "video_section", "ic_video_section"),
    VOICE_TO_SIGN("Secțiune voce-semn", "voice_to_sign_section", "ic_voice_to_sign_section");

    private final String title;
    private final String mediaKey;
    private final String iconKey;
}
