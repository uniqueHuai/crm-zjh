package com.crm.collaboration.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.framework.security.SecurityUtils;
import com.crm.collaboration.entity.CrmApprovalInstance;
import com.crm.collaboration.service.ICrmApprovalInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "审批实例", description = "发起审批、审批操作、撤销")
@RestController
@RequestMapping("/approval-instances")
@RequiredArgsConstructor
public class CrmApprovalInstanceController {

    private final ICrmApprovalInstanceService instanceService;

    @Operation(summary = "审批实例列表")
    @GetMapping
    @PreAuthorize("hasAuthority('collaboration:approval-instance:list')")
    public R<IPage<CrmApprovalInstance>> list(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String bizType,
                                              @RequestParam(required = false) Long applicantId,
                                              @RequestParam(required = false) String keywords,
                                              @RequestParam(required = false) String dateFrom,
                                              @RequestParam(required = false) String dateTo) {
        return R.ok(instanceService.selectPageWithCondition(new Page<>(page, size), status, bizType, applicantId, keywords, dateFrom, dateTo));
    }

    @Operation(summary = "发起审批")
    @PostMapping
    @PreAuthorize("hasAuthority('collaboration:approval-instance:create')")
    @OperationLog(module = "collaboration", action = "create", description = "发起审批")
    public R<Void> create(@RequestBody CrmApprovalInstance instance) {
        instanceService.save(instance);
        return R.ok();
    }

    @Operation(summary = "审批详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:approval-instance:query')")
    public R<CrmApprovalInstance> get(@PathVariable Long id) {
        return R.ok(instanceService.selectWithRecords(id));
    }

    @Operation(summary = "待我审批列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('collaboration:approval-instance:list')")
    public R<IPage<CrmApprovalInstance>> pending(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size,
                                                  @RequestParam Long approverId) {
        return R.ok(instanceService.selectPending(new Page<>(page, size), approverId));
    }

    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('collaboration:approval-instance:approve')")
    @OperationLog(module = "collaboration", action = "approve", description = "审批通过")
    public R<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long approverId = SecurityUtils.getUserId();
        String comment = body != null ? (String) body.get("comment") : null;
        String signatureImage = body != null ? (String) body.get("signatureImage") : null;
        instanceService.approve(id, approverId, comment, signatureImage);
        return R.ok();
    }

    @Operation(summary = "审批驳回")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('collaboration:approval-instance:reject')")
    @OperationLog(module = "collaboration", action = "reject", description = "审批驳回")
    public R<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long approverId = SecurityUtils.getUserId();
        String comment = body != null ? (String) body.get("comment") : null;
        instanceService.reject(id, approverId, comment);
        return R.ok();
    }

    @Operation(summary = "撤销审批")
    @PutMapping("/{id}/recall")
    @PreAuthorize("hasAuthority('collaboration:approval-instance:recall')")
    @OperationLog(module = "collaboration", action = "recall", description = "撤销审批")
    public R<Void> recall(@PathVariable Long id) {
        instanceService.recall(id);
        return R.ok();
    }
}
