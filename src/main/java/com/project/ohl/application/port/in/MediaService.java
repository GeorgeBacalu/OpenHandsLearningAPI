package com.project.ohl.application.port.in;

import com.project.ohl.application.model.MediaDownload;
import java.io.InputStream;

public interface MediaService {
    void upload(String type, String mediaKey, String contentType, long size, InputStream content);
    MediaDownload download(String type, String mediaKey);
    void delete(String type, String mediaKey);
}
