package com.crm.ai.controller;

import com.crm.ai.agent.AgentRouter;
import com.crm.ai.agent.BaseAgent;
import com.crm.ai.conversation.entity.Conversation;
import com.crm.ai.conversation.service.ConversationService;
import com.crm.common.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 智能客服 — 小程序端使用（免登录）
 */
@Slf4j
@Tag(name = "智能客服")
@RestController
@RequestMapping("/ai/app/customer-service")
@RequiredArgsConstructor
public class CustomerServiceChatController {

    private final AgentRouter agentRouter;
    private final ConversationService conversationService;
    private final ExecutorService aiExecutor = Executors.newCachedThreadPool();

    @Operation(summary = "智能客服聊天（SSE 流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, Object> body) {
        SseEmitter emitter = new SseEmitter(0L);
        Long conversationId = body.get("conversationId") != null
                ? Long.valueOf(body.get("conversationId").toString()) : null;
        Long customerId = body.get("customerId") != null
                ? Long.valueOf(body.get("customerId").toString()) : null;
        String message = (String) body.get("message");

        BaseAgent agent = agentRouter.getAgent("customer_service");
        aiExecutor.execute(() -> {
            try {
                agent.chat(conversationId, null, customerId, message, emitter);
            } catch (Exception e) {
                log.error("Customer service chat error", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Operation(summary = "获取历史会话列表")
    @GetMapping("/conversations")
    public R<List<Conversation>> getConversations(@RequestParam Long customerId) {
        return R.ok(conversationService.getCustomerConversations(customerId));
    }
}
