package com.iarecruiter.company.domain.port;

import java.io.InputStream;

public interface StoragePort {
    String uploadFile(String bucket, String objectKey, InputStream data, long size, String contentType);
    void deleteFile(String bucket, String objectKey);
    String getPresignedUrl(String bucket, String objectKey, int expirySeconds);
}
