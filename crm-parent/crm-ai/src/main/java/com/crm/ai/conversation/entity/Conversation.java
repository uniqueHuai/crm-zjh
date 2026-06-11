package com.crm.ai.conversation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_conversation")
public class Conversation {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String agentType;    // customer_service / sales_assistant / butler
    private String title;
    private Long userId;         // 内部用户ID（销售/管家）
    private Long customerId;     // 小程序客户ID（客服）
    private String status;       // active / closed
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
