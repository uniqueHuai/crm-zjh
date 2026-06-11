package com.crm.ai.knowledge.service;

import com.crm.ai.knowledge.entity.KnowledgeChunk;
import com.crm.ai.knowledge.mapper.KnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量检索服务 — 根据用户问题在知识库中检索最相关的文档块
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkMapper knowledgeChunkMapper;

    /**
     * 在指定知识库中检索最相关的 K 个文档块
     */
    public List<KnowledgeChunk> search(Long kbId, String query, int topK) {
        // 1. 将用户问题转为向量
        List<Double> queryVector = embeddingService.embed(query);
        if (queryVector.isEmpty()) {
            log.warn("Failed to embed query: {}", query);
            return List.of();
        }

        // 2. 向量检索
        String vectorStr = embeddingService.toVectorString(queryVector);
        return knowledgeChunkMapper.findSimilar(kbId, vectorStr, topK);
    }

    /**
     * 检索并格式化为上下文文本
     */
    public String searchAsContext(Long kbId, String query, int topK) {
        List<KnowledgeChunk> chunks = search(kbId, query, topK);
        return chunks.stream()
                .map(c -> c.getTitle() != null ? "【" + c.getTitle() + "】\n" + c.getContent()
                        : c.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 跨所有知识库检索
     */
    public List<KnowledgeChunk> searchAll(String query, int topK) {
        List<Double> queryVector = embeddingService.embed(query);
        if (queryVector.isEmpty()) return List.of();

        String vectorStr = embeddingService.toVectorString(queryVector);
        // 在所有知识库中检索：传递 kbId=null 的场景用全库搜索
        // 由于 MyBatis XML 需要动态处理，这里简化：遍历所有启用知识库
        return List.of(); // 简化版本，实际可按需扩展
    }
}
