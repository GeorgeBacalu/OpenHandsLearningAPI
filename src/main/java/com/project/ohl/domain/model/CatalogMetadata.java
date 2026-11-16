package com.project.ohl.domain.model;

import com.project.ohl.domain.exception.InvalidRequestException;

public final class CatalogMetadata {
    public static final String IMAGE_URL_PATTERN =
            "https://[a-z0-9][a-z0-9.-]+\\.s3\\.[a-z0-9-]+\\.amazonaws\\.com/(?:dev|prod)/categories/(?:[a-z0-9_]+/)+[a-z0-9_]+\\.webp";
    public static final String VIDEO_URL_PATTERN =
            "https://[a-z0-9][a-z0-9.-]+\\.s3\\.[a-z0-9-]+\\.amazonaws\\.com/(?:dev|prod)/categories/(?:[a-z0-9_]+/)+[a-z0-9_]+\\.mp4";

    private CatalogMetadata() {}

    public static String imageUrl(String requested, String current) {
        String result = requested == null ? current : requested;
        if (result == null || result.length() > 2048 || !result.matches(IMAGE_URL_PATTERN)) {
            throw new InvalidRequestException("Image URL must point to an HTTPS S3 image object.");
        }
        return result;
    }

    public static String videoUrl(String requested, String current) {
        String result = requested == null ? current : requested;
        if (result != null && (result.length() > 2048 || !result.matches(VIDEO_URL_PATTERN))) {
            throw new InvalidRequestException("Video URL must point to an HTTPS S3 MP4 object.");
        }
        return result;
    }
}
