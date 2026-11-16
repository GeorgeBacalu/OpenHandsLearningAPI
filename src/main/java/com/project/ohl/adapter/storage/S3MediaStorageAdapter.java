package com.project.ohl.adapter.storage;

import com.project.ohl.application.model.MediaDownload;
import com.project.ohl.application.port.out.MediaStoragePort;
import com.project.ohl.domain.exception.MediaStorageException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3MediaStorageAdapter implements MediaStoragePort {
    private final S3Client client;
    private final String bucket;
    private final String region;

    public S3MediaStorageAdapter(S3Client client,
            @Value("${app.storage.s3.bucket}") String bucket,
            @Value("${app.storage.s3.region}") String region) {
        this.client = client;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public void upload(String key, String contentType, long size, InputStream content) {
        try {
            client.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                    .contentType(contentType).contentLength(size).build(), RequestBody.fromInputStream(content, size));
        } catch (SdkException exception) {
            throw new MediaStorageException(exception);
        }
    }

    @Override
    public MediaDownload download(String key) {
        return new MediaDownload(key, "https://" + bucket + ".s3." + region
                + ".amazonaws.com/" + key);
    }

    @Override
    public void delete(String key) {
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException exception) {
            throw new MediaStorageException(exception);
        }
    }
}
