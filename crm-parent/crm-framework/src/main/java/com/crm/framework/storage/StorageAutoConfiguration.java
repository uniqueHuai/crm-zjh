package com.crm.framework.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储自动配置
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "crm.storage.type", havingValue = "minio", matchIfMissing = false)
    public FileStorage minioFileStorage(StorageProperties properties) {
        return new MinioFileStorage(properties);
    }

    @Bean
    @ConditionalOnMissingBean(FileStorage.class)
    public FileStorage localFileStorage(StorageProperties properties) {
        return new LocalFileStorage(properties);
    }
}
