package com.example.fileservice.domain.port.outbound;

import java.io.InputStream;

public interface StoragePort {
    void upload(String bucket, String storageKey, InputStream data, long size, String contentType) throws Exception;
    InputStream download(String bucket, String storageKey) throws Exception;
    void delete(String bucket, String storageKey) throws Exception;
    boolean exists(String bucket, String storageKey) throws Exception;
    String getPresignedUrl(String bucket, String storageKey, int expirySeconds) throws Exception;
}
