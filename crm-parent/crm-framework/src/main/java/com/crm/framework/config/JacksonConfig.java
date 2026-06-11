package com.crm.framework.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Long 转 String，防止前端精度丢失
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            module.addSerializer(BigInteger.class, ToStringSerializer.instance);
            module.addSerializer(BigDecimal.class, ToStringSerializer.instance);

            builder.modules(module);

            // JSR310 时间序列化
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // LocalDateTime -> yyyy-MM-dd HH:mm:ss
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
            // LocalDate -> yyyy-MM-dd
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(df));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(df));
            // 注册 JSR310 模块（兜底其他类型如 LocalTime、ZonedDateTime）
            builder.modulesToInstall(JavaTimeModule.class);

            // 忽略未知字段
            builder.failOnUnknownProperties(false);

            // 空对象不抛异常
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        };
    }
}
