package com.crm.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.ai.knowledge.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    @Select(value = """
        SELECT id, kb_id, title, content, chunk_index, metadata, token_count, created_at,
               1 - (embedding <=> CAST(#{embedding} AS vector)) AS similarity
        FROM ai_knowledge_chunk
        WHERE kb_id = #{kbId}
          AND embedding IS NOT NULL
        ORDER BY embedding <=> CAST(#{embedding} AS vector)
        LIMIT #{topK}
        """)
    List<KnowledgeChunk> findSimilar(@Param("kbId") Long kbId,
                                     @Param("embedding") String embedding,
                                     @Param("topK") int topK);
}
