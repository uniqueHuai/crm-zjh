package com.crm.ai.conversation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.ai.conversation.entity.Conversation;
import com.crm.ai.conversation.entity.Message;
import com.crm.ai.conversation.mapper.ConversationMapper;
import com.crm.ai.conversation.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    private final MessageMapper messageMapper;

    @Transactional(rollbackFor = Exception.class)
    public Conversation createConversation(String agentType, Long userId, Long customerId) {
        Conversation conv = new Conversation();
        conv.setAgentType(agentType);
        conv.setTitle("新对话");
        conv.setUserId(userId);
        conv.setCustomerId(customerId);
        conv.setStatus("active");
        conv.setMessageCount(0);
        save(conv);
        return conv;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addMessage(Long conversationId, String role, String content, String toolCalls,
                           Integer tokensIn, Integer tokensOut, Integer latencyMs) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setToolCalls(toolCalls);
        msg.setTokensIn(tokensIn != null ? tokensIn : 0);
        msg.setTokensOut(tokensOut != null ? tokensOut : 0);
        msg.setLatencyMs(latencyMs != null ? latencyMs : 0);
        messageMapper.insert(msg);

        lambdaUpdate()
                .eq(Conversation::getId, conversationId)
                .setSql("message_count = message_count + 1")
                .update();
    }

    public List<Message> getMessages(Long conversationId) {
        return messageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreatedAt)
        );
    }

    public List<Conversation> getUserConversations(String agentType, Long userId) {
        return lambdaQuery()
                .eq(Conversation::getAgentType, agentType)
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdatedAt)
                .list();
    }

    public List<Conversation> getCustomerConversations(Long customerId) {
        return lambdaQuery()
                .eq(Conversation::getAgentType, "customer_service")
                .eq(Conversation::getCustomerId, customerId)
                .orderByDesc(Conversation::getUpdatedAt)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void closeConversation(Long id) {
        lambdaUpdate()
                .eq(Conversation::getId, id)
                .set(Conversation::getStatus, "closed")
                .update();
    }
}
