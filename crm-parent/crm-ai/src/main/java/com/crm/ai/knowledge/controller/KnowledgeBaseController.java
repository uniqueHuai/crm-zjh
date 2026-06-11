package com.crm.ai.knowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.ai.knowledge.entity.KnowledgeBase;
import com.crm.ai.knowledge.service.KnowledgeBaseService;
import com.crm.common.model.R;
import com.crm.system.service.ISysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/ai/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final ISysFileService sysFileService;

    @Operation(summary = "知识库分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('ai:knowledge:list')")
    public R<Page<KnowledgeBase>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) String keywords) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (keywords != null && !keywords.isEmpty()) {
            wrapper.like(KnowledgeBase::getName, keywords);
        }
        wrapper.orderByDesc(KnowledgeBase::getCreatedAt);
        return R.ok(knowledgeBaseService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "知识库详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ai:knowledge:list')")
    public R<KnowledgeBase> getById(@PathVariable Long id) {
        return R.ok(knowledgeBaseService.getById(id));
    }

    @Operation(summary = "新增知识库")
    @PostMapping
    @PreAuthorize("hasAuthority('ai:knowledge:create')")
    public R<Void> create(@RequestBody KnowledgeBase kb) {
        knowledgeBaseService.save(kb);
        return R.ok();
    }

    @Operation(summary = "编辑知识库")
    @PutMapping
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    public R<Void> update(@RequestBody KnowledgeBase kb) {
        knowledgeBaseService.updateById(kb);
        return R.ok();
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ai:knowledge:delete')")
    public R<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "导入文档到知识库（文本）")
    @PostMapping("/{id}/import")
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    public R<Void> importDocument(@PathVariable Long id, @RequestBody Map<String, String> body) {
        knowledgeBaseService.importDocument(id, body.get("title"), body.get("content"));
        return R.ok();
    }

    @Operation(summary = "导入文档文件到知识库（支持 .txt/.md/.doc/.docx/.pdf）")
    @PostMapping("/{id}/import-file")
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    public R<Void> importFile(@PathVariable Long id,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(required = false) String title) throws Exception {
        // 1. 存储原始文件到对象存储
        sysFileService.uploadFile(file, "knowledge_base", true);

        // 2. 读取文本内容
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return R.failed(400, "文件名不能为空");
        }

        String lowerName = originalName.toLowerCase();
        String content;

        if (lowerName.endsWith(".txt") || lowerName.endsWith(".md")) {
            // 纯文本文件 — 直接读取
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            content = sb.toString();
        } else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
            // Word 文件 — 使用 Apache POI 提取文本
            try (var inputStream = file.getInputStream()) {
                var extractor = ExtractorFactory.createExtractor(inputStream);
                content = extractor.getText();
                extractor.close();
            }
        } else if (lowerName.endsWith(".pdf")) {
            // PDF 文件 — 使用 PDFBox 提取文本
            try (PDDocument document = PDDocument.load(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                content = stripper.getText(document);
            }
        } else {
            return R.failed(400, "仅支持 .txt、.md、.doc/.docx 和 .pdf 文件导入");
        }

        // 3. 分块向量化
        String docTitle = title != null ? title : originalName;
        knowledgeBaseService.importDocument(id, docTitle, content);
        return R.ok();
    }

    @Operation(summary = "获取所有启用知识库")
    @GetMapping("/enabled")
    public R<java.util.List<KnowledgeBase>> getEnabled() {
        return R.ok(knowledgeBaseService.getEnabledList());
    }
}
