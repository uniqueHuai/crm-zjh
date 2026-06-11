package com.crm.ai.llm;

import com.crm.ai.config.AiConfig;
import com.crm.ai.llm.dto.ChatCompletionChunk;
import com.crm.ai.llm.dto.ChatCompletionRequest;
import com.crm.ai.llm.dto.ChatCompletionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiliconFlowLlmService implements LlmService {

    private final AiConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_ENDPOINT = "/chat/completions";

    @Override
    public ChatCompletionResponse call(ChatCompletionRequest request) {
        request.setStream(false);
        HttpEntity<ChatCompletionRequest> entity = buildRequestEntity(request);

        ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                aiConfig.getBaseUrl() + CHAT_ENDPOINT,
                HttpMethod.POST,
                entity,
                ChatCompletionResponse.class
        );

        return response.getBody();
    }

    @Override
    public void callStream(ChatCompletionRequest request, SseEmitter emitter) {
        request.setStream(true);
        HttpEntity<ChatCompletionRequest> entity = buildRequestEntity(request);

        try {
            restTemplate.execute(
                    aiConfig.getBaseUrl() + CHAT_ENDPOINT,
                    HttpMethod.POST,
                    clientHttpRequest -> {
                        clientHttpRequest.getHeaders().putAll(entity.getHeaders());
                        objectMapper.writeValue(clientHttpRequest.getBody(), request);
                    },
                    clientHttpResponse -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        clientHttpResponse.getBody(),
                                        StandardCharsets.UTF_8
                                ))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String trimmed = line.trim();
                                if (!trimmed.startsWith("data:")) continue;

                                String data = trimmed.substring(5).trim();
                                if ("[DONE]".equals(data)) break;

                                try {
                                    ChatCompletionChunk chunk = objectMapper.readValue(data, ChatCompletionChunk.class);
                                    if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) continue;

                                    ChatCompletionChunk.Delta delta = chunk.getChoices().get(0).getDelta();
                                    String finishReason = chunk.getChoices().get(0).getFinishReason();

                                    if (delta != null) {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(objectMapper.writeValueAsString(delta)));
                                    }

                                    if ("stop".equals(finishReason)) break;
                                    if ("tool_calls".equals(finishReason)) {
                                        emitter.send(SseEmitter.event().name("tool_calls").data("[TOOL_CALLS]"));
                                        break;
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to parse SSE chunk: {}", data, e);
                                }
                            }
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            emitter.complete();
                            return null;
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Stream request failed", e);
            emitter.completeWithError(e);
        }
    }

    @Override
    public void callStream(ChatCompletionRequest request, Consumer<String> onChunk,
                           Runnable onComplete, Consumer<Throwable> onError) {
        request.setStream(true);
        HttpEntity<ChatCompletionRequest> entity = buildRequestEntity(request);

        try {
            restTemplate.execute(
                    aiConfig.getBaseUrl() + CHAT_ENDPOINT,
                    HttpMethod.POST,
                    clientHttpRequest -> {
                        clientHttpRequest.getHeaders().putAll(entity.getHeaders());
                        objectMapper.writeValue(clientHttpRequest.getBody(), request);
                    },
                    clientHttpResponse -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        clientHttpResponse.getBody(),
                                        StandardCharsets.UTF_8
                                ))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String trimmed = line.trim();
                                if (!trimmed.startsWith("data:")) continue;

                                String data = trimmed.substring(5).trim();
                                if ("[DONE]".equals(data)) {
                                    onComplete.run();
                                    return null;
                                }

                                try {
                                    ChatCompletionChunk chunk = objectMapper.readValue(data, ChatCompletionChunk.class);
                                    if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) continue;

                                    ChatCompletionChunk.Delta delta = chunk.getChoices().get(0).getDelta();
                                    String finishReason = chunk.getChoices().get(0).getFinishReason();

                                    if (delta.getContent() != null) {
                                        onChunk.accept(objectMapper.writeValueAsString(delta));
                                    }

                                    // Forward tool_calls in the delta via a special signal
                                    if (delta.getToolCalls() != null && !delta.getToolCalls().isEmpty()) {
                                        onChunk.accept("{\"_type\":\"tool_calls\",\"data\":"
                                            + objectMapper.writeValueAsString(delta.getToolCalls()) + "}");
                                    }

                                    if ("stop".equals(finishReason)) {
                                        onChunk.accept("{\"_type\":\"finish\",\"reason\":\"stop\"}");
                                        onComplete.run();
                                        return null;
                                    }
                                    if ("tool_calls".equals(finishReason)) {
                                        onChunk.accept("{\"_type\":\"finish\",\"reason\":\"tool_calls\"}");
                                        onComplete.run();
                                        return null;
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to parse SSE chunk: {}", data, e);
                                }
                            }
                            onComplete.run();
                            return null;
                        } catch (Exception e) {
                            onError.accept(e);
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Stream request failed", e);
            onError.accept(e);
        }
    }

    private HttpEntity<ChatCompletionRequest> buildRequestEntity(ChatCompletionRequest request) {
        if (request.getModel() == null) request.setModel(aiConfig.getModel());
        if (request.getTemperature() == null) request.setTemperature(aiConfig.getTemperature());
        if (request.getMaxTokens() == null) request.setMaxTokens(aiConfig.getMaxTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getApiKey());

        return new HttpEntity<>(request, headers);
    }
}
