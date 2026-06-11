package com.crm.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_knowledge_chunk")
public class KnowledgeChunk {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long kbId;
    private String title;
    private String content;
    private Integer chunkIndex;
    private Object embedding; // pgvector VECTOR(1536) - MyBatis-Plus handles as Object
    private String metadata;
    private Integer tokenCount;
    private LocalDateTime createdAt;
}
