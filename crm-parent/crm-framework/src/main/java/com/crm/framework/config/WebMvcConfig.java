package com.crm.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 本地文件访问映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${crm.storage.local-path:./uploads}")
    private String localPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到本地文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + localPath + "/");
    }
}
