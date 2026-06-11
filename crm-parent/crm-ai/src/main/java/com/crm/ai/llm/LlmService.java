package com.crm.ai.llm;

import com.crm.ai.llm.dto.ChatCompletionRequest;
import com.crm.ai.llm.dto.ChatCompletionResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.function.Consumer;

public interface LlmService {

    /**
     * 非流式调用 LLM
     */
    ChatCompletionResponse call(ChatCompletionRequest request);

    /**
     * 流式调用 LLM，逐块推送到 SseEmitter
     */
    void callStream(ChatCompletionRequest request, SseEmitter emitter);

    /**
     * 流式调用 LLM，逐块回调 Consumer
     */
    void callStream(ChatCompletionRequest request, Consumer<String> onChunk, Runnable onComplete, Consumer<Throwable> onError);
}
