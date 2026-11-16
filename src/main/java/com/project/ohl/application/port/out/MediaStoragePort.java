package com.project.ohl.application.port.out;

import com.project.ohl.application.model.MediaDownload;
import java.io.InputStream;

public interface MediaStoragePort {
    void upload(String key, String contentType, long size, InputStream content);
    MediaDownload download(String key);
    void delete(String key);
}
