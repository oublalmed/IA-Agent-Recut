package com.iarecruiter.candidate.domain.port;

import java.io.InputStream;

public interface ResumeStoragePort {
    String upload(String objectKey, InputStream data, long size, String contentType);
    InputStream download(String objectKey);
    void delete(String objectKey);
    String getPresignedUrl(String objectKey, int expirySeconds);
}
