package com.crm.framework.storage;

import io.minio.*;
import io.minio.http.Method;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储实现
 */
@Slf4j
public class MinioFileStorage implements FileStorage {

    private final MinioClient minioClient;
    private final StorageProperties.Minio config;
    private final String basePath;

    public MinioFileStorage(StorageProperties properties) {
        this.config = properties.getMinio();
        this.minioClient = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .region(config.getRegion())
                .build();
        this.basePath = config.getBucket();
    }

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(config.getBucket()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(config.getBucket()).build());
                log.info("Created MinIO bucket: {}", config.getBucket());
            }
            log.info("MinIO storage initialized: {} bucket={}", config.getEndpoint(), config.getBucket());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO storage", e);
        }
    }

    @Override
    public String store(String fileName, byte[] content, String contentType) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(fileName)
                    .stream(bais, content.length, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            return "/minio/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file to MinIO: " + fileName, e);
        }
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        String objectName = filePath.startsWith("/minio/") ? filePath.substring(7) : filePath;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete MinIO object: {}", filePath, e);
        }
    }

    @Override
    public String getDownloadUrl(String filePath, long expiresIn) {
        String objectName = filePath.startsWith("/minio/") ? filePath.substring(7) : filePath;
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(Math.toIntExact(expiresIn > 0 ? expiresIn : 3600), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to generate presigned URL, fallback to path: {}", filePath, e);
            return filePath;
        }
    }

    @Override
    public InputStream getContent(String filePath) {
        String objectName = filePath.startsWith("/minio/") ? filePath.substring(7) : filePath;
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("File not found in MinIO: " + filePath, e);
        }
    }

    @Override
    public String getType() {
        return "minio";
    }
}
