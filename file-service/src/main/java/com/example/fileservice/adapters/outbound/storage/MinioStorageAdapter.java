package com.example.fileservice.adapters.outbound.storage;

import com.example.fileservice.domain.port.outbound.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;
    private final String defaultBucketName;
    private final int defaultExpirySeconds;

    public MinioStorageAdapter(MinioClient minioClient,
                               @org.springframework.beans.factory.annotation.Value("${minio.bucket.name:files}") String defaultBucketName,
                               @org.springframework.beans.factory.annotation.Value("${minio.url-expiry-seconds:3600}") int defaultExpirySeconds) {
        this.minioClient = minioClient;
        this.defaultBucketName = defaultBucketName;
        this.defaultExpirySeconds = defaultExpirySeconds;
    }

    @Override
    public void upload(String bucket, String storageKey, InputStream data, long size, String contentType) throws Exception {
        // ensure bucket exists is optional - MinioService in previous implementation did that; we try create if not exists
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .stream(data, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public InputStream download(String bucket, String storageKey) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(storageKey).build());
    }

    @Override
    public void delete(String bucket, String storageKey) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(storageKey).build());
    }

    @Override
    public boolean exists(String bucket, String storageKey) throws Exception {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(storageKey).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String storageKey, int expirySeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(storageKey)
                .expiry(expirySeconds, TimeUnit.SECONDS)
                .build());
    }
}
