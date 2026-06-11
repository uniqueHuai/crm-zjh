package com.crm.collaboration.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.collaboration.entity.CrmApprovalDefine;
import com.crm.collaboration.service.ICrmApprovalDefineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "审批流程定义", description = "审批流 CRUD、启用停用")
@RestController
@RequestMapping("/approval-defines")
@RequiredArgsConstructor
public class CrmApprovalDefineController {

    private final ICrmApprovalDefineService defineService;

    @Operation(summary = "流程定义列表")
    @GetMapping
    @PreAuthorize("hasAuthority('collaboration:approval-define:list')")
    public R<IPage<CrmApprovalDefine>> list(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size,
                                            @RequestParam(required = false) String bizType,
                                            @RequestParam(required = false) String keywords) {
        LambdaQueryWrapper<CrmApprovalDefine> wrapper = new LambdaQueryWrapper<CrmApprovalDefine>()
                .eq(bizType != null, CrmApprovalDefine::getBizType, bizType)
                .like(keywords != null, CrmApprovalDefine::getName, keywords)
                .orderByDesc(CrmApprovalDefine::getCreatedAt);
        return R.ok(defineService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "新增流程定义")
    @PostMapping
    @PreAuthorize("hasAuthority('collaboration:approval-define:create')")
    @OperationLog(module = "collaboration", action = "create", description = "新增审批流程定义")
    public R<Void> create(@RequestBody CrmApprovalDefine define) {
        defineService.save(define);
        return R.ok();
    }

    @Operation(summary = "流程详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:approval-define:query')")
    public R<CrmApprovalDefine> get(@PathVariable Long id) {
        return R.ok(defineService.getById(id));
    }

    @Operation(summary = "更新流程定义")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:approval-define:edit')")
    @OperationLog(module = "collaboration", action = "update", description = "更新审批流程定义")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmApprovalDefine define) {
        define.setId(id);
        defineService.updateById(define);
        return R.ok();
    }

    @Operation(summary = "删除流程定义")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:approval-define:delete')")
    @OperationLog(module = "collaboration", action = "delete", description = "删除审批流程定义")
    public R<Void> delete(@PathVariable Long id) {
        defineService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "启用/停用")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('collaboration:approval-define:edit')")
    @OperationLog(module = "collaboration", action = "update", description = "启用/停用审批流程")
    public R<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        defineService.lambdaUpdate().eq(CrmApprovalDefine::getId, id)
                .set(CrmApprovalDefine::getStatus, body.get("status"))
                .update();
        return R.ok();
    }
}
