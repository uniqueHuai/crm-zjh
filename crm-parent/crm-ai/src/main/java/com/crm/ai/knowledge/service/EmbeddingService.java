package com.crm.ai.knowledge.service;

import com.crm.ai.config.AiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 文本向量化服务（复用 SiliconFlow 的 embedding 模型）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final AiConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String EMBEDDING_ENDPOINT = "/embeddings";

    /**
     * 将文本转为向量
     */
    public List<Double> embed(String text) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "BAAI/bge-m3");
            body.put("input", text);
            body.put("encoding_format", "float");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiConfig.getBaseUrl() + EMBEDDING_ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode embedding = data.get(0).get("embedding");
                List<Double> result = new ArrayList<>();
                for (JsonNode val : embedding) {
                    result.add(val.asDouble());
                }
                return result;
            }
        } catch (Exception e) {
            log.error("Embedding failed for text: {}", text.substring(0, Math.min(50, text.length())), e);
        }
        return Collections.emptyList();
    }

    /**
     * 将向量转为 pgvector 可用的 SQL 字符串
     */
    public String toVectorString(List<Double> vector) {
        if (vector == null || vector.isEmpty()) return null;
        return "[" + String.join(",", vector.stream().map(String::valueOf).toArray(String[]::new)) + "]";
    }
}
