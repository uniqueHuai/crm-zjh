package com.crm.ai.knowledge.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.ai.knowledge.entity.KnowledgeBase;
import com.crm.ai.knowledge.entity.KnowledgeChunk;
import com.crm.ai.knowledge.mapper.KnowledgeBaseMapper;
import com.crm.ai.knowledge.mapper.KnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;

    /**
     * 导入文档内容（分块 + 向量化）
     */
    @Transactional(rollbackFor = Exception.class)
    public void importDocument(Long kbId, String title, String content) {
        // 简单分块：按段落分割
        String[] paragraphs = content.split("\\n\\s*\\n");
        int index = 0;
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setKbId(kbId);
            chunk.setTitle(index == 0 ? title : null);
            chunk.setContent(trimmed);
            chunk.setChunkIndex(index++);
            chunk.setTokenCount(trimmed.length() / 2); // 粗略估算

            // 向量化
            try {
                java.util.List<Double> vector = embeddingService.embed(trimmed);
                if (!vector.isEmpty()) {
                    chunk.setEmbedding(embeddingService.toVectorString(vector));
                }
            } catch (Exception e) {
                // 向量化失败时继续，后续可重试
            }

            knowledgeChunkMapper.insert(chunk);
        }
    }

    /**
     * 检索相关文档块
     */
    public List<KnowledgeChunk> searchRelated(Long kbId, String query, int topK) {
        return vectorSearchService.search(kbId, query, topK);
    }

    /**
     * 检索所有启用知识库
     */
    public List<KnowledgeBase> getEnabledList() {
        return lambdaQuery().eq(KnowledgeBase::getStatus, "enabled").list();
    }
}
