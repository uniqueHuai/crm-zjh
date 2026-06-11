package com.crm.framework.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置
 */
@Data
@ConfigurationProperties(prefix = "crm.storage")
public class StorageProperties {

    /** 存储类型：local（默认） | minio */
    private String type = "local";

    /** 本地存储路径（type=local 时生效） */
    private String localPath = "./uploads";

    /** MinIO 配置（type=minio 时生效） */
    private Minio minio = new Minio();

    @Data
    public static class Minio {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "crm-files";
        private String region = "";
    }
}
