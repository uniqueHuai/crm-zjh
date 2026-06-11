package com.crm.customer.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmLeadDistributionRule;
import com.crm.customer.service.ICrmLeadDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "客户分配规则", description = "线索分配规则 CRUD、手动触发执行、分配日志查看")
@RestController
@RequestMapping("/lead-distribution-rules")
@RequiredArgsConstructor
public class CrmLeadDistributionController {

    private final ICrmLeadDistributionService distributionService;

    @Operation(summary = "分配规则列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:distribution:list')")
    public R<List<CrmLeadDistributionRule>> list() {
        return R.ok(distributionService.lambdaQuery()
                .orderByAsc(CrmLeadDistributionRule::getPriority)
                .list());
    }

    @Operation(summary = "新增分配规则")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:distribution:create')")
    @OperationLog(module = "customer", action = "create", description = "新增分配规则")
    public R<Void> create(@RequestBody CrmLeadDistributionRule rule) {
        distributionService.save(rule);
        return R.ok();
    }

    @Operation(summary = "更新分配规则")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:distribution:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新分配规则")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmLeadDistributionRule rule) {
        rule.setId(id);
        distributionService.updateById(rule);
        return R.ok();
    }

    @Operation(summary = "删除分配规则")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:distribution:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除分配规则")
    public R<Void> delete(@PathVariable Long id) {
        distributionService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "手动触发规则执行")
    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAuthority('customer:distribution:execute')")
    @OperationLog(module = "customer", action = "execute", description = "触发分配规则执行")
    public R<Void> execute(@PathVariable Long id) {
        distributionService.executeRule(id);
        return R.ok();
    }

    @Operation(summary = "查看分配日志")
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('customer:distribution:query')")
    public R<Map<String, Object>> logs(@PathVariable Long id,
                                        @RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "20") long size) {
        return R.ok(distributionService.selectLogsByRuleId(id, page, size));
    }
}
