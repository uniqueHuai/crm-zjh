package com.crm.ai.agent;

import com.crm.ai.agent.tools.ToolExecutor;
import com.crm.ai.conversation.service.ConversationService;
import com.crm.ai.knowledge.service.KnowledgeBaseService;
import com.crm.ai.knowledge.service.VectorSearchService;
import com.crm.ai.llm.LlmService;
import com.crm.ai.llm.dto.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能客服 Agent — 基于知识库回答小程序用户问题
 */
@Component
public class CustomerServiceAgent extends BaseAgent {

    private final KnowledgeBaseService knowledgeBaseService;
    private final VectorSearchService vectorSearchService;

    public CustomerServiceAgent(LlmService llmService,
                                ConversationService conversationService,
                                ToolExecutor toolExecutor,
                                ObjectMapper objectMapper,
                                KnowledgeBaseService knowledgeBaseService,
                                VectorSearchService vectorSearchService) {
        super(llmService, conversationService, toolExecutor, objectMapper);
        this.knowledgeBaseService = knowledgeBaseService;
        this.vectorSearchService = vectorSearchService;
    }

    @Override
    public String getAgentType() {
        return "customer_service";
    }

    @Override
    protected String getSystemPrompt() {
        return "你是 CRM 智能客服助手\"小C\"，基于知识库回答客户问题。\n\n" +
                "规则：\n" +
                "1. 只回答知识库范围内的问题，不知道就说\"这个问题我需要转接人工客服\"\n" +
                "2. 回答要简洁、友好、专业\n" +
                "3. 涉及订单/售后问题时，引导用户提供订单号\n" +
                "4. 不能透露任何公司内部信息\n" +
                "5. 不能编造商品信息或政策条款\n\n" +
                "当用户情绪激动或问题超出范围时，主动触发转人工流程。";
    }

    @Override
    protected List<ToolDefinition> getTools() {
        return List.of();
    }

    /**
     * 智能客服：根据用户问题检索知识库作为上下文
     */
    @Override
    protected String getExtraContext(Long userId, String userMessage) {
        var kbList = knowledgeBaseService.getEnabledList();
        if (kbList.isEmpty()) return null;

        StringBuilder context = new StringBuilder();
        for (var kb : kbList) {
            String result = vectorSearchService.searchAsContext(kb.getId(), userMessage, 3);
            if (!result.isEmpty()) {
                context.append("【").append(kb.getName()).append("】\n").append(result).append("\n\n");
            }
        }
        return context.length() > 0 ? context.toString() : null;
    }
}
