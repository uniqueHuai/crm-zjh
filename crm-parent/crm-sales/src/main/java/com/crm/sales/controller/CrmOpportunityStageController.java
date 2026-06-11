package com.crm.sales.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmOpportunityStage;
import com.crm.sales.service.ICrmOpportunityStageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "销售阶段定义", description = "商机阶段定义 CRUD")
@RestController
@RequestMapping("/opportunity-stages")
@RequiredArgsConstructor
public class CrmOpportunityStageController {

    private final ICrmOpportunityStageService stageService;

    @Operation(summary = "阶段定义列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:stage:list')")
    public R<List<CrmOpportunityStage>> list() {
        return R.ok(stageService.lambdaQuery()
                .orderByAsc(CrmOpportunityStage::getSortOrder)
                .list());
    }

    @Operation(summary = "新增阶段")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:stage:create')")
    @OperationLog(module = "sales", action = "create", description = "新增销售阶段")
    public R<Void> create(@RequestBody CrmOpportunityStage stage) {
        stageService.save(stage);
        return R.ok();
    }

    @Operation(summary = "更新阶段")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:stage:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新销售阶段")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmOpportunityStage stage) {
        stage.setId(id);
        stageService.updateById(stage);
        return R.ok();
    }

    @Operation(summary = "删除阶段")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:stage:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除销售阶段")
    public R<Void> delete(@PathVariable Long id) {
        stageService.removeById(id);
        return R.ok();
    }
}
