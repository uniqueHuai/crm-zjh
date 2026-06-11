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
 * 智能管家 — 管理层使用（需登录 + manager/admin 角色）
 */
@Slf4j
@Tag(name = "智能管家")
@RestController
@RequestMapping("/ai/butler")
@RequiredArgsConstructor
public class ButlerChatController {

    private final AgentRouter agentRouter;
    private final ConversationService conversationService;
    private final ExecutorService aiExecutor = Executors.newCachedThreadPool();

    @Operation(summary = "智能管家聊天（SSE 流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('ai:butler:chat')")
    public SseEmitter chat(@RequestBody Map<String, Object> body) {
        SseEmitter emitter = new SseEmitter(0L);
        Long conversationId = body.get("conversationId") != null
                ? Long.valueOf(body.get("conversationId").toString()) : null;
        String message = (String) body.get("message");
        Long userId = SecurityUtils.getUserId();

        BaseAgent agent = agentRouter.getAgent("butler");
        aiExecutor.execute(() -> {
            try {
                agent.chat(conversationId, userId, null, message, emitter);
            } catch (Exception e) {
                log.error("Butler chat error", e);
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
        return R.ok(conversationService.getUserConversations("butler", userId));
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
