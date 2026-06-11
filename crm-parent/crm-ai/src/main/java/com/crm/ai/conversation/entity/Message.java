package com.crm.ai.conversation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_message")
public class Message {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long conversationId;
    private String role;     // user / assistant / system / tool
    private String content;
    private String toolCalls; // JSON
    private Integer tokensIn;
    private Integer tokensOut;
    private Integer latencyMs;
    private LocalDateTime createdAt;
}
