package com.crm.framework.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储 — 存储到服务器磁盘
 */
@Slf4j
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {

    private final StorageProperties properties;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(properties.getLocalPath()));
            log.info("Local storage initialized at {}", properties.getLocalPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create local storage directory", e);
        }
    }

    @Override
    public String store(String fileName, byte[] content, String contentType) {
        Path targetPath = Paths.get(properties.getLocalPath(), fileName);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file locally: " + fileName, e);
        }
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        String fileName = filePath.startsWith("/uploads/") ? filePath.substring(9) : filePath;
        try {
            Files.deleteIfExists(Paths.get(properties.getLocalPath(), fileName));
        } catch (IOException e) {
            log.warn("Failed to delete local file: {}", filePath, e);
        }
    }

    @Override
    public String getDownloadUrl(String filePath, long expiresIn) {
        // 本地存储直接返回路径，由 static resource mapping 提供访问
        return filePath;
    }

    @Override
    public InputStream getContent(String filePath) {
        String fileName = filePath.startsWith("/uploads/") ? filePath.substring(9) : filePath;
        try {
            return Files.newInputStream(Paths.get(properties.getLocalPath(), fileName));
        } catch (IOException e) {
            throw new RuntimeException("File not found: " + filePath, e);
        }
    }

    @Override
    public String getType() {
        return "local";
    }
}
