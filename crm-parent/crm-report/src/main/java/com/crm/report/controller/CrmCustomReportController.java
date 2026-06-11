package com.crm.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.report.entity.CrmCustomReport;
import com.crm.report.service.ICrmCustomReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "自定义报表", description = "报表 CRUD、数据查询、导出")
@RestController
@RequestMapping("/reports/custom")
@RequiredArgsConstructor
public class CrmCustomReportController {

    private final ICrmCustomReportService reportService;

    @Operation(summary = "报表列表")
    @GetMapping
    @PreAuthorize("hasAuthority('report:custom-report:list')")
    public R<IPage<CrmCustomReport>> list(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size,
                                          @RequestParam(required = false) String keywords,
                                          @RequestParam(required = false) String chartType) {
        return R.ok(reportService.selectPage(new Page<>(page, size), keywords, chartType));
    }

    @Operation(summary = "创建报表")
    @PostMapping
    @PreAuthorize("hasAuthority('report:custom-report:create')")
    @OperationLog(module = "report", action = "create", description = "创建自定义报表")
    public R<Void> create(@RequestBody CrmCustomReport report) {
        reportService.save(report);
        return R.ok();
    }

    @Operation(summary = "报表数据")
    @GetMapping("/{id}/data")
    @PreAuthorize("hasAuthority('report:custom-report:query')")
    public R<Map<String, Object>> data(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size) {
        return R.ok(reportService.getReportData(id, page, size));
    }

    @Operation(summary = "报表导出")
    @PostMapping("/{id}/export")
    @PreAuthorize("hasAuthority('report:custom-report:export')")
    @OperationLog(module = "report", action = "export", description = "导出自定义报表")
    public R<Void> export(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reportService.exportReport(id, body.get("format"), body.get("startDate"), body.get("endDate"));
        return R.ok();
    }

    @Operation(summary = "更新报表")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('report:custom-report:edit')")
    @OperationLog(module = "report", action = "update", description = "更新自定义报表")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmCustomReport report) {
        report.setId(id);
        reportService.updateById(report);
        return R.ok();
    }

    @Operation(summary = "删除报表")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('report:custom-report:delete')")
    @OperationLog(module = "report", action = "delete", description = "删除自定义报表")
    public R<Void> delete(@PathVariable Long id) {
        reportService.removeById(id);
        return R.ok();
    }
}
