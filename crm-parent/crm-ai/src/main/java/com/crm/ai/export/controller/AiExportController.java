package com.crm.ai.export.controller;

import com.crm.ai.export.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Tag(name = "AI 导出")
@RestController
@RequestMapping("/ai/export")
@RequiredArgsConstructor
public class AiExportController {

    private final ReportExportService exportService;

    @Operation(summary = "下载导出的报表文件")
    @GetMapping("/{token}")
    public void download(@PathVariable String token, HttpServletResponse response) {
        ReportExportService.ExportFile ef = exportService.getExportFile(token);
        if (ef == null) {
            response.setStatus(404);
            return;
        }

        File file = new File(ef.filePath());
        if (!file.exists()) {
            exportService.removeToken(token);
            response.setStatus(404);
            return;
        }

        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String encoded = URLEncoder.encode(ef.displayName(), StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encoded);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));

            try (InputStream is = new FileInputStream(file)) {
                is.transferTo(response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            log.error("Export download failed: token={}", token, e);
            response.setStatus(500);
        }
    }
}
