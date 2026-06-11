package com.crm.report.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.report.service.ICrmReportDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "核心看板", description = "销售漏斗、客户分析、员工效能、商城经营、看板布局")
@RestController
@RequestMapping("/report/dashboard")
@RequiredArgsConstructor
public class CrmDashboardController {

    private final ICrmReportDashboardService dashboardService;

    @Operation(summary = "销售漏斗数据")
    @GetMapping("/sales-funnel")
    @PreAuthorize("hasAuthority('report:dashboard:query')")
    public R<Map<String, Object>> salesFunnel(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate,
                                              @RequestParam(required = false) Long ownerId,
                                              @RequestParam(required = false) Long deptId) {
        return R.ok(dashboardService.salesFunnel(startDate, endDate, ownerId, deptId));
    }

    @Operation(summary = "客户分析")
    @GetMapping("/customer-analysis")
    @PreAuthorize("hasAuthority('report:dashboard:query')")
    public R<Map<String, Object>> customerAnalysis(@RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        return R.ok(dashboardService.customerAnalysis(startDate, endDate));
    }

    @Operation(summary = "员工效能")
    @GetMapping("/employee-performance")
    @PreAuthorize("hasAuthority('report:dashboard:query')")
    public R<Map<String, Object>> employeePerformance(@RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate,
                                                      @RequestParam(required = false) Long deptId) {
        return R.ok(dashboardService.employeePerformance(startDate, endDate, deptId));
    }

    @Operation(summary = "商城经营数据")
    @GetMapping("/mall")
    @PreAuthorize("hasAuthority('report:dashboard:query')")
    public R<Map<String, Object>> mall(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return R.ok(dashboardService.mall(startDate, endDate));
    }

    @Operation(summary = "看板卡片模板配置")
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('report:dashboard:query')")
    public R<?> templates() {
        return R.ok(dashboardService.templates());
    }

    @Operation(summary = "看板汇总数据")
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('report:dashboard:query')")
    public R<Map<String, Object>> summary() {
        return R.ok(dashboardService.summary());
    }

    @Operation(summary = "保存看板布局")
    @PutMapping("/layout")
    @PreAuthorize("hasAuthority('report:dashboard:edit')")
    @OperationLog(module = "report", action = "update", description = "保存看板布局")
    public R<Void> saveLayout(@RequestParam Long userId, @RequestBody List<Object> cards) {
        dashboardService.saveLayout(userId, cards);
        return R.ok();
    }
}
