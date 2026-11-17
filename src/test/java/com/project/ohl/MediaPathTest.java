package com.project.ohl;

import com.project.ohl.application.port.out.MediaStoragePort;
import com.project.ohl.application.service.MediaServiceImpl;
import com.project.ohl.domain.exception.InvalidRequestException;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class MediaPathTest {
    @Test
    void developmentUsesRootKeysForDownloadsUploadsAndDeletes() {
        var storage = mock(MediaStoragePort.class);
        var media = new MediaServiceImpl(storage);
        var content = new ByteArrayInputStream(new byte[]{1});

        media.download("images", "categories/alfabet/a/main.webp");
        media.download("images", "voice/a.webp");
        media.upload("videos", "categories/alfabet/a/main.mp4", "video/mp4", 1, content);
        media.delete("videos", "categories/alfabet/a/main.mp4");

        verify(storage).download("categories/alfabet/a/main.webp");
        verify(storage).download("voice/a.webp");
        verify(storage).upload("categories/alfabet/a/main.mp4", "video/mp4", 1, content);
        verify(storage).delete("categories/alfabet/a/main.mp4");
    }

    @Test
    void mediaKeysAreRelativeToTheBucketRoot() {
        var storage = mock(MediaStoragePort.class);
        var media = new MediaServiceImpl(storage);

        media.download("images", "categories/obiecte/magazin/produse/ardei/main.webp");
        media.download("videos", "categories/alfabet/a_breve/main.mp4");
        media.download("images", "voice/a_breve.webp");
        media.download("images", "categories/main.webp");
        media.download("images", "categories/header.webp");

        verify(storage).download("categories/obiecte/magazin/produse/ardei/main.webp");
        verify(storage).download("categories/alfabet/a_breve/main.mp4");
        verify(storage).download("voice/a_breve.webp");
        verify(storage).download("categories/main.webp");
        verify(storage).download("categories/header.webp");
    }

    @Test
    void rejectsTraversalWrongExtensionsAndForeignEnvironmentPrefixes() {
        var storage = mock(MediaStoragePort.class);
        var media = new MediaServiceImpl(storage);

        for (String key : new String[]{"categories/../a.webp", "prod/categories/alfabet/a/main.webp", "dev/categories/alfabet/a/main.webp",
                "categories/alfabet/a.png", "categories/alfabet/a/main.webp/", "voice/a.mp4", "categories//a.webp"}) {
            assertThatThrownBy(() -> media.download("images", key)).isInstanceOf(InvalidRequestException.class);
        }
        verifyNoInteractions(storage);
    }
}
