package com.crm.ai.agent;

import com.crm.ai.agent.tools.ToolExecutor;
import com.crm.ai.conversation.entity.Conversation;
import com.crm.ai.conversation.entity.Message;
import com.crm.ai.conversation.service.ConversationService;
import com.crm.ai.llm.LlmService;
import com.crm.ai.llm.dto.ChatCompletionRequest;
import com.crm.ai.llm.dto.ChatMessage;
import com.crm.ai.llm.dto.ToolDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Agent 基类 — 流式优先，Tool Calling 自动处理
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseAgent {

    protected final LlmService llmService;
    protected final ConversationService conversationService;
    protected final ToolExecutor toolExecutor;
    protected final ObjectMapper objectMapper;

    public abstract String getAgentType();
    protected abstract String getSystemPrompt();
    protected abstract List<ToolDefinition> getTools();

    /**
     * 获取额外上下文（如知识库检索结果），子类可重写
     */
    protected String getExtraContext(Long userId, String userMessage) {
        return null;
    }

    /**
     * 流式处理用户消息 — 首轮即流式，检测到 tool_calls 自动执行后继续流式
     */
    public void chat(Long conversationId, Long userId, Long customerId, String userMessage, SseEmitter emitter) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取或创建对话
            Conversation conv;
            if (conversationId != null) {
                conv = conversationService.getById(conversationId);
                if (conv == null) {
                    emitter.send(SseEmitter.event().name("error").data("对话不存在"));
                    emitter.complete();
                    return;
                }
            } else {
                conv = conversationService.createConversation(getAgentType(), userId, customerId);
                emitter.send(SseEmitter.event().name("conv_id")
                        .data("{\"conv_id\":" + conv.getId() + "}"));
            }

            // 2. 保存用户消息
            conversationService.addMessage(conv.getId(), "user", userMessage, null, 0, 0, 0);

            // 3. 自动生成对话标题
            if (conv.getMessageCount() == 0 || conv.getMessageCount() == 1) {
                String title = userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage;
                conversationService.lambdaUpdate()
                        .eq(Conversation::getId, conv.getId())
                        .set(Conversation::getTitle, title)
                        .update();
            }

            // 4. 构建消息列表
            List<ChatMessage> messages = buildMessageList(conv.getId(), userMessage);

            // 5. 流式处理（支持 tool_calls 自动循环执行）
            streamWithTools(conv, messages, userId, emitter, startTime);

        } catch (Exception e) {
            log.error("Agent chat error", e);
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
            } catch (IOException ignored) {}
            emitter.completeWithError(e);
        }
    }

    /**
     * 保存 assistant 回复到数据库（空内容也保存，确保用户退出时不丢失）
     */
    private void saveAssistantResponse(Long conversationId, StringBuilder contentBuffer, long startTime) {
        String content = contentBuffer.length() > 0
                ? contentBuffer.toString()
                : "[流式响应完成]";
        long latency = System.currentTimeMillis() - startTime;
        conversationService.addMessage(conversationId, "assistant", content, null, 0, 0, (int) latency);
    }

    /**
     * 流式调用 LLM，自动处理 tool_calls 循环
     */
    private void streamWithTools(Conversation conv, List<ChatMessage> messages,
                                  Long userId, SseEmitter emitter, long startTime) {

        // 收集流式内容
        StringBuilder contentBuffer = new StringBuilder();
        java.util.Map<String, ChatMessage.ToolCall> collectedToolCallMap = new java.util.LinkedHashMap<>();
        AtomicBoolean hasToolCalls = new AtomicBoolean(false);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(messages)
                .tools(getTools())
                .stream(true)
                .build();

        llmService.callStream(request,
                deltaJson -> {
                    try {
                        // 检测是否是控制信号
                        if (deltaJson.contains("\"_type\"")) {
                            // tool_calls 信号
                            if (deltaJson.contains("\"tool_calls\"")) {
                                Map<String, Object> signal = objectMapper.readValue(deltaJson,
                                        new TypeReference<Map<String, Object>>() {});
                                if ("tool_calls".equals(signal.get("_type"))) {
                                    String dataJson = objectMapper.writeValueAsString(signal.get("data"));
                                    List<ChatMessage.ToolCall> tcs = objectMapper.readValue(dataJson,
                                            new TypeReference<List<ChatMessage.ToolCall>>() {});
                                    // 按 id 合并流式 tool_calls 片段（流式会分多个 chunk 发送同一个 tool_call 的片段）
                                    for (ChatMessage.ToolCall tc : tcs) {
                                        String tcId = tc.getId();
                                        if (tcId == null || tcId.isEmpty()) tcId = "_no_id";
                                        ChatMessage.ToolCall existing = collectedToolCallMap.get(tcId);
                                        if (existing == null) {
                                            collectedToolCallMap.put(tcId, tc);
                                        } else if (tc.getFunction() != null) {
                                            // 合并 function 字段
                                            if (tc.getFunction().getName() != null && !tc.getFunction().getName().isEmpty()) {
                                                existing.getFunction().setName(tc.getFunction().getName());
                                            }
                                            if (tc.getFunction().getArguments() != null && !tc.getFunction().getArguments().isEmpty()) {
                                                String merged = (existing.getFunction().getArguments() != null
                                                        ? existing.getFunction().getArguments() : "")
                                                        + tc.getFunction().getArguments();
                                                existing.getFunction().setArguments(merged);
                                            }
                                        }
                                    }
                                    hasToolCalls.set(true);
                                }
                            }
                            // finish 信号 — 忽略，onComplete 会处理
                            return;
                        }

                        // 正常的 content delta — 立即推送到前端
                        emitter.send(SseEmitter.event().name("message").data(deltaJson));

                        // 累积内容
                        try {
                            Map<String, Object> delta = objectMapper.readValue(deltaJson,
                                    new TypeReference<Map<String, Object>>() {});
                            Object content = delta.get("content");
                            if (content != null) {
                                contentBuffer.append(content.toString());
                            }
                        } catch (Exception ignored) {}

                    } catch (Exception e) {
                        log.warn("Failed to forward delta", e);
                        try {
                            emitter.send(SseEmitter.event().name("message").data(deltaJson));
                        } catch (IOException ignored) {}
                    }
                },
                () -> {
                    // 过滤出有效的 tool_calls（跳过流式合并产生的幽灵调用）
                    List<ChatMessage.ToolCall> validToolCalls = collectedToolCallMap.values().stream()
                            .filter(tc -> tc.getFunction() != null
                                    && tc.getFunction().getName() != null
                                    && !tc.getFunction().getName().isEmpty())
                            .collect(java.util.stream.Collectors.toList());

                    // 流完成 — 检查是否需要执行工具
                    if (hasToolCalls.get() && !validToolCalls.isEmpty()) {

                        // 构建完整消息链：原始消息 + assistant 的 tool_calls + tool 结果
                        List<ChatMessage> toolMessages = new ArrayList<>(messages);

                        // 1. 加入 assistant 的 tool_calls 消息（必须，符合 API 格式）
                        toolMessages.add(ChatMessage.builder()
                                .role("assistant")
                                .toolCalls(validToolCalls)
                                .build());

                        // 2. 执行工具并加入 tool 结果消息（带 tool_call_id）
                        for (ChatMessage.ToolCall tc : validToolCalls) {
                            log.info("Executing tool: {} with args: {}",
                                    tc.getFunction().getName(), tc.getFunction().getArguments());
                            String result = toolExecutor.execute(
                                    tc.getFunction().getName(),
                                    tc.getFunction().getArguments(),
                                    userId
                            );
                            toolMessages.add(ChatMessage.builder()
                                    .role("tool")
                                    .toolCallId(tc.getId())
                                    .content(result)
                                    .build());
                        }

                        // 3. 最后加一条引导提示
                        toolMessages.add(ChatMessage.builder()
                                .role("user")
                                .content("请基于以上查询结果，用中文回答我的问题。")
                                .build());

                        StringBuilder secondContent = new StringBuilder();
                        ChatCompletionRequest secondRequest = ChatCompletionRequest.builder()
                                .messages(toolMessages)
                                .stream(true)
                                .build();

                        llmService.callStream(secondRequest,
                                deltaJson2 -> {
                                    try {
                                        emitter.send(SseEmitter.event().name("message").data(deltaJson2));
                                        // 累积内容
                                        try {
                                            Map<String, Object> delta = objectMapper.readValue(deltaJson2,
                                                    new TypeReference<Map<String, Object>>() {});
                                            Object content = delta.get("content");
                                            if (content != null) secondContent.append(content.toString());
                                        } catch (Exception ignored) {}
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                () -> {
                                    saveAssistantResponse(conv.getId(), secondContent, startTime);
                                    try {
                                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                        emitter.complete();
                                    } catch (IOException ignored) {}
                                },
                                error -> {
                                    // 流异常时也保存已累积的内容，避免用户退出后丢失
                                    saveAssistantResponse(conv.getId(), secondContent, startTime);
                                    try {
                                        emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                                    } catch (IOException ignored) {}
                                    emitter.completeWithError(error);
                                }
                        );

                    } else {
                        // 无工具调用 — 完成
                        saveAssistantResponse(conv.getId(), contentBuffer, startTime);
                        try {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            emitter.complete();
                        } catch (IOException ignored) {}
                    }
                },
                error -> {
                    log.error("Stream error", error);
                    // 流异常时也保存已累积的内容，避免用户退出后丢失
                    saveAssistantResponse(conv.getId(), contentBuffer, startTime);
                    try {
                        emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                    } catch (IOException ignored) {}
                    emitter.completeWithError(error);
                }
        );
    }

    private List<ChatMessage> buildMessageList(Long conversationId, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        String systemContent = getSystemPrompt();
        String extraContext = getExtraContext(null, userMessage);
        if (extraContext != null) {
            systemContent += "\n\n以下是相关的知识库内容，请基于这些内容回答：\n\n" + extraContext;
        }
        messages.add(ChatMessage.builder()
                .role("system")
                .content(systemContent)
                .build());

        List<Message> history = conversationService.getMessages(conversationId);
        for (Message msg : history) {
            if ("user".equals(msg.getRole()) || "assistant".equals(msg.getRole())) {
                String content = msg.getContent();
                if (content != null && !content.isEmpty() && !content.startsWith("[工具") && !content.startsWith("[流式")) {
                    messages.add(ChatMessage.builder()
                            .role(msg.getRole())
                            .content(content)
                            .build());
                }
            }
        }

        messages.add(ChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());

        return messages;
    }
}
