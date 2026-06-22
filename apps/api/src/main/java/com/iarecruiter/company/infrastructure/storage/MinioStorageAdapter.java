package com.iarecruiter.company.infrastructure.storage;

import com.iarecruiter.company.domain.port.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;

    @Override
    public String uploadFile(String bucket, String objectKey, InputStream data, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(data, size, -1)
                    .contentType(contentType)
                    .build());
            return getPresignedUrl(bucket, objectKey, 3600 * 24 * 7);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String bucket, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO bucket={} key={}: {}", bucket, objectKey, e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String objectKey, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }
}
