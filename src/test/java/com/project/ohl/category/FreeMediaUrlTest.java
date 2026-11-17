package com.project.ohl.category;

import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.CatalogMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreeMediaUrlTest {
    private static final String BASE = "https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/open-hands-learning/categories/alfabet/a/main";

    @Test
    void approvedFreeMediaWorksAndExistingS3CatalogsStillWork() {
        assertThat(CatalogMetadata.imageUrl(BASE + ".webp", null)).isEqualTo(BASE + ".webp");
        assertThat(CatalogMetadata.videoUrl(BASE + ".mp4", null)).isEqualTo(BASE + ".mp4");
        String s3 = "https://open-hands-learning.s3.us-east-1.amazonaws.com/categories/alfabet/a/main.webp";
        assertThat(CatalogMetadata.imageUrl(s3, null)).isEqualTo(s3);
        String video = s3.replace(".webp", ".mp4");
        assertThat(CatalogMetadata.videoUrl(video, null)).isEqualTo(video);
    }

    @Test
    void otherBucketsHostsSchemesAndObjectTypesStayRejected() {
        for (String invalid : new String[]{BASE.replace("open-hands-learning", "private-documents") + ".webp",
                BASE.replace("https:", "http:") + ".webp",
                BASE.replace("br-red-base-b9fi1yrl", "another-project") + ".webp", BASE + ".html"}) {
            assertThatThrownBy(() -> CatalogMetadata.imageUrl(invalid, null)).isInstanceOf(InvalidRequestException.class);
        }
        assertThatThrownBy(() -> CatalogMetadata.videoUrl(BASE + ".webp", null)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> CatalogMetadata.imageUrl(BASE.replace("/categories/", "/staging/categories/") + ".webp", null))
                .isInstanceOf(InvalidRequestException.class);
    }
}
