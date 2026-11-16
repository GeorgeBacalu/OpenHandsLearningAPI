package com.project.ohl;

import com.project.ohl.application.port.out.MediaStoragePort;
import com.project.ohl.application.service.MediaServiceImpl;
import com.project.ohl.domain.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class MediaPathTest {
    @Test
    void environmentIsConfigurationAndCannotBeOverriddenByAnObjectPath() {
        var storage = mock(MediaStoragePort.class);
        var media = new MediaServiceImpl(storage, "prod");
        media.download("images", "categories/obiecte/magazin/produse/ardei.webp");
        media.download("videos", "categories/alfabet/a_breve.mp4");
        media.download("images", "voice/voice_a_breve.webp");
        verify(storage).download("prod/categories/obiecte/magazin/produse/ardei.webp");
        verify(storage).download("prod/categories/alfabet/a_breve.mp4");
        verify(storage).download("prod/voice/voice_a_breve.webp");
        assertThatThrownBy(() -> new MediaServiceImpl(storage, "staging"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTraversalWrongExtensionsAndForeignEnvironmentPrefixes() {
        var storage = mock(MediaStoragePort.class);
        var media = new MediaServiceImpl(storage, "dev");
        for (String key : new String[]{"categories/../a.webp", "prod/categories/alfabet/a.webp",
                "categories/alfabet/a.png", "categories/alfabet/a.webp/", "voice/a.mp4", "categories//a.webp"}) {
            assertThatThrownBy(() -> media.download("images", key)).isInstanceOf(InvalidRequestException.class);
        }
        verifyNoInteractions(storage);
    }
}
