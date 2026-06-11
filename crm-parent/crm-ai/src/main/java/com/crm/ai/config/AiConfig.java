package com.crm.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "siliconflow")
public class AiConfig {
    private String apiKey;
    private String baseUrl = "https://api.siliconflow.cn/v1";
    private String model = "deepseek-ai/DeepSeek-V4-Flash";
    private double temperature = 0.7;
    private int maxTokens = 4096;
}
