package com.iarecruiter.candidate.infrastructure.storage;

import com.iarecruiter.candidate.domain.port.ResumeStoragePort;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioResumeStorageAdapter implements ResumeStoragePort {
    private final MinioClient minioClient;

    @Value("${minio.bucket-resumes}")
    private String bucket;

    @Override
    public String upload(String objectKey, InputStream data, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(objectKey)
                    .stream(data, size, -1).contentType(contentType).build());
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            log.error("MinIO delete failed for {}: {}", objectKey, e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String objectKey, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).bucket(bucket).object(objectKey)
                    .expiry(expirySeconds, TimeUnit.SECONDS).build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO presigned URL failed: " + e.getMessage(), e);
        }
    }
}
