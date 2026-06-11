package com.crm.ai.controller;

import com.crm.ai.agent.AgentRouter;
import com.crm.ai.agent.BaseAgent;
import com.crm.ai.conversation.entity.Conversation;
import com.crm.ai.conversation.entity.Message;
import com.crm.ai.conversation.service.ConversationService;
import com.crm.common.model.R;
import com.crm.framework.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 销售助手 — 公司员工使用（需登录）
 */
@Slf4j
@Tag(name = "销售助手")
@RestController
@RequestMapping("/ai/sales-assistant")
@RequiredArgsConstructor
public class SalesAssistantChatController {

    private final AgentRouter agentRouter;
    private final ConversationService conversationService;
    private final ExecutorService aiExecutor = Executors.newCachedThreadPool();

    @Operation(summary = "销售助手聊天（SSE 流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('ai:assistant:chat')")
    public SseEmitter chat(@RequestBody Map<String, Object> body) {
        SseEmitter emitter = new SseEmitter(0L);
        Long conversationId = body.get("conversationId") != null
                ? Long.valueOf(body.get("conversationId").toString()) : null;
        String message = (String) body.get("message");
        Long userId = SecurityUtils.getUserId();

        // 必须在异步线程处理，否则 SseEmitter 无法流式输出
        BaseAgent agent = agentRouter.getAgent("sales_assistant");
        aiExecutor.execute(() -> {
            try {
                agent.chat(conversationId, userId, null, message, emitter);
            } catch (Exception e) {
                log.error("Sales assistant chat error", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Operation(summary = "获取我的历史会话列表")
    @GetMapping("/conversations")
    public R<List<Conversation>> getConversations() {
        Long userId = SecurityUtils.getUserId();
        return R.ok(conversationService.getUserConversations("sales_assistant", userId));
    }

    @Operation(summary = "获取会话消息详情")
    @GetMapping("/conversations/{id}/messages")
    public R<List<Message>> getMessages(@PathVariable Long id) {
        return R.ok(conversationService.getMessages(id));
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/conversations/{id}/close")
    public R<Void> closeConversation(@PathVariable Long id) {
        conversationService.closeConversation(id);
        return R.ok();
    }
}
