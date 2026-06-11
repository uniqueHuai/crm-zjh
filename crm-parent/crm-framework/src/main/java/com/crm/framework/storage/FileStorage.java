package com.crm.framework.storage;

import java.io.InputStream;

/**
 * 文件存储抽象接口 — 支持本地存储 / MinIO / OSS
 */
public interface FileStorage {

    /** 存储文件 */
    String store(String fileName, byte[] content, String contentType);

    /** 删除文件 */
    void delete(String filePath);

    /** 获取文件下载 URL（可指定过期时间，秒） */
    String getDownloadUrl(String filePath, long expiresIn);

    /** 获取文件内容 */
    InputStream getContent(String filePath);

    /** 存储类型标识：local / minio */
    String getType();
}
