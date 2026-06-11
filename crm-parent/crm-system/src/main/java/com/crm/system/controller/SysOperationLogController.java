package com.crm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysOperationLog;
import com.crm.system.service.ISysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "操作日志", description = "系统操作日志查询与清理")
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final ISysOperationLogService logService;

    @Operation(summary = "日志分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:log:list')")
    public R<IPage<SysOperationLog>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "20") long size,
                                           @RequestParam(required = false) String module,
                                           @RequestParam(required = false) String action,
                                           @RequestParam(required = false) Long operatorId,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        return R.ok(logService.selectPageWithCondition(new Page<>(page, size), module, action, operatorId, startDate, endDate));
    }

    @Operation(summary = "日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:log:query')")
    public R<SysOperationLog> get(@PathVariable Long id) {
        return R.ok(logService.getById(id));
    }

    @Operation(summary = "清理日志")
    @DeleteMapping("/clean")
    @PreAuthorize("hasAuthority('system:log:delete')")
    @OperationLog(module = "system", action = "delete", description = "清理操作日志")
    public R<Void> clean(@RequestBody Map<String, Integer> body) {
        int beforeDays = body.getOrDefault("beforeDays", 180);
        logService.cleanLogs(beforeDays);
        return R.ok();
    }

    @Operation(summary = "导出日志")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:log:export')")
    public R<Void> export(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        // TODO: async export to Excel
        return R.ok();
    }
}
