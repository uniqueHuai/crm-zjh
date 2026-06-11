package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmOpportunity;
import com.crm.sales.service.ICrmOpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商机管理", description = "商机 CRUD、阶段推进、赢单/输单、看板、参与人管理")
@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
public class CrmOpportunityController {

    private final ICrmOpportunityService opportunityService;

    @Operation(summary = "商机分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:opportunity:list')")
    public R<IPage<CrmOpportunity>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size,
                                          @RequestParam(required = false) Long stageId,
                                          @RequestParam(required = false) Long customerId,
                                          @RequestParam(required = false) Long ownerId,
                                          @RequestParam(required = false) String keywords,
                                          @RequestParam(required = false) Boolean isExpired) {
        return R.ok(opportunityService.selectPageWithCondition(new Page<>(page, size), stageId, customerId,
                ownerId, keywords, isExpired));
    }

    @Operation(summary = "新增商机")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:opportunity:create')")
    @OperationLog(module = "sales", action = "create", description = "新增商机")
    public R<Void> create(@RequestBody CrmOpportunity opportunity) {
        opportunityService.save(opportunity);
        return R.ok();
    }

    @Operation(summary = "商机详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:opportunity:query')")
    public R<CrmOpportunity> get(@PathVariable Long id) {
        return R.ok(opportunityService.selectWithDetail(id));
    }

    @Operation(summary = "更新商机")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:opportunity:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新商机")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmOpportunity opportunity) {
        opportunity.setId(id);
        opportunityService.updateById(opportunity);
        return R.ok();
    }

    @Operation(summary = "删除商机")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:opportunity:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除商机")
    public R<Void> delete(@PathVariable Long id) {
        opportunityService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "拖拽更新阶段")
    @PutMapping("/{id}/stage")
    @PreAuthorize("hasAuthority('sales:opportunity:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新商机阶段")
    public R<Void> updateStage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long stageId = Long.valueOf(body.get("stageId").toString());
        String remark = (String) body.get("remark");
        opportunityService.updateStage(id, stageId, remark);
        return R.ok();
    }

    @Operation(summary = "赢单")
    @PutMapping("/{id}/win")
    @PreAuthorize("hasAuthority('sales:opportunity:edit')")
    @OperationLog(module = "sales", action = "update", description = "商机赢单")
    public R<Void> win(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        opportunityService.win(id, params);
        return R.ok();
    }

    @Operation(summary = "输单")
    @PutMapping("/{id}/lose")
    @PreAuthorize("hasAuthority('sales:opportunity:edit')")
    @OperationLog(module = "sales", action = "update", description = "商机输单")
    public R<Void> lose(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        opportunityService.lose(id, params);
        return R.ok();
    }

    @Operation(summary = "商机看板数据")
    @GetMapping("/pipeline")
    @PreAuthorize("hasAuthority('sales:opportunity:query')")
    public R<List<Map<String, Object>>> pipeline() {
        return R.ok(opportunityService.pipeline());
    }

    @Operation(summary = "添加商机参与人")
    @PostMapping("/{id}/participants")
    @PreAuthorize("hasAuthority('sales:opportunity:edit')")
    @OperationLog(module = "sales", action = "update", description = "添加商机参与人")
    public R<Void> addParticipants(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = ((List<Integer>) body.get("userIds")).stream().map(Long::valueOf).toList();
        opportunityService.addParticipants(id, userIds);
        return R.ok();
    }

    @Operation(summary = "移除商机参与人")
    @DeleteMapping("/{id}/participants")
    @PreAuthorize("hasAuthority('sales:opportunity:edit')")
    @OperationLog(module = "sales", action = "update", description = "移除商机参与人")
    public R<Void> removeParticipants(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = ((List<Integer>) body.get("userIds")).stream().map(Long::valueOf).toList();
        opportunityService.removeParticipants(id, userIds);
        return R.ok();
    }
}
