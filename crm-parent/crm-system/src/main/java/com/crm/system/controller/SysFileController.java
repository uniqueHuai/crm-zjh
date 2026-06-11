package com.crm.system.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysFile;
import com.crm.system.service.ISysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "文件管理", description = "文件上传、下载、删除")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class SysFileController {

    private final ISysFileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    @OperationLog(module = "system", action = "create", description = "上传文件")
    public R<SysFile> upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) String bizType,
                             @RequestParam(required = false) Boolean isPublic) {
        return R.ok(fileService.uploadFile(file, bizType, isPublic));
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{id}")
    public R<SysFile> get(@PathVariable Long id) {
        return R.ok(fileService.getById(id));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        SysFile sysFile = fileService.getById(id);
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }
        String filename = sysFile.getOriginalName() != null ? sysFile.getOriginalName() : sysFile.getFileName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileService.downloadFile(id));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @OperationLog(module = "system", action = "delete", description = "删除文件")
    @PreAuthorize("hasAuthority('system:file:delete')")
    public R<Void> delete(@PathVariable Long id) {
        fileService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "获取文件下载链接")
    @GetMapping("/{id}/download-url")
    public R<Map<String, String>> downloadUrl(@PathVariable Long id,
                                               @RequestParam(defaultValue = "3600") Long expiresIn) {
        String url = fileService.getDownloadUrl(id, expiresIn);
        return R.ok(Map.of("url", url));
    }

    @Operation(summary = "获取文件列表（按业务关联）")
    @GetMapping
    public R<List<SysFile>> list(@RequestParam(required = false) String bizType,
                                  @RequestParam(required = false) Long bizId) {
        return R.ok(fileService.selectByBiz(bizType, bizId));
    }
}
