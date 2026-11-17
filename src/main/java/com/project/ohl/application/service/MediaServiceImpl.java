package com.project.ohl.application.service;

import com.project.ohl.application.model.MediaDownload;
import com.project.ohl.application.port.in.MediaService;
import com.project.ohl.application.port.out.MediaStoragePort;
import com.project.ohl.domain.exception.InvalidRequestException;
import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaStoragePort storage;

    public MediaServiceImpl(MediaStoragePort storage) {
        this.storage = storage;
    }

    @Override
    public void upload(String type, String mediaKey, String contentType, long size, InputStream content) {
        String key = key(type, mediaKey);
        String expected = type.equals("images") ? "image/webp" : "video/mp4";
        if (size <= 0 || content == null || !expected.equals(contentType)) {
            throw new InvalidRequestException("Upload a nonempty file with a supported content type for " + type + ".");
        }
        storage.upload(key, contentType, size, content);
    }

    @Override
    public MediaDownload download(String type, String mediaKey) {
        return storage.download(key(type, mediaKey));
    }

    @Override
    public void delete(String type, String mediaKey) {
        storage.delete(key(type, mediaKey));
    }

    private String key(String type, String mediaKey) {
        if (!("images".equals(type) || "videos".equals(type))) {
            throw new InvalidRequestException("Media type must be images or videos.");
        }
        String suffix = "images".equals(type) ? "webp" : "mp4";
        boolean category = mediaKey != null && mediaKey.matches("categories/(?:[a-z0-9_]+/)*[a-z0-9_]+\\." + suffix);
        boolean voice = "images".equals(type) && mediaKey != null && mediaKey.matches("voice/[a-z0-9_]+\\.webp");
        if (mediaKey == null || mediaKey.length() > 1000 || !(category || voice)) {
            throw new InvalidRequestException("Invalid media key.");
        }
        return mediaKey;
    }
}
