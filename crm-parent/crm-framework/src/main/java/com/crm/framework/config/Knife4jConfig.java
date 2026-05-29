package com.crm.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CRM 系统 API 文档")
                        .version("1.0.0")
                        .description("CRM 客户关系管理系统接口文档，基于 Spring Boot 3.x + MyBatis-Plus")
                        .contact(new Contact().name("开发团队").email("dev@crm.com"))
                        .license(new License().name("Apache 2.0")));
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1-系统管理")
                .packagesToScan("com.crm.system.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
                .group("2-客户管理")
                .packagesToScan("com.crm.customer.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi salesApi() {
        return GroupedOpenApi.builder()
                .group("3-销售管理")
                .packagesToScan("com.crm.sales.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi mallApi() {
        return GroupedOpenApi.builder()
                .group("4-商城管理")
                .packagesToScan("com.crm.mall.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi collaborationApi() {
        return GroupedOpenApi.builder()
                .group("5-协同办公")
                .packagesToScan("com.crm.collaboration.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi reportApi() {
        return GroupedOpenApi.builder()
                .group("6-数据分析")
                .packagesToScan("com.crm.report.controller")
                .build();
    }
}
