package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmContract;
import com.crm.sales.service.ICrmContractService;
import com.crm.system.entity.SysFile;
import com.crm.system.service.ISysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "合同管理", description = "合同 CRUD、签署、作废、续签、附件")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class CrmContractController {

    private final ICrmContractService contractService;
    private final ISysFileService sysFileService;

    @Operation(summary = "合同分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:contract:list')")
    public R<IPage<CrmContract>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) String keywords,
                                       @RequestParam(required = false) Long customerId,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return R.ok(contractService.selectPageWithCondition(new Page<>(page, size), keywords, customerId, status, startDate, endDate));
    }

    @Operation(summary = "新增合同")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:contract:create')")
    @OperationLog(module = "sales", action = "create", description = "新增合同")
    public R<Void> create(@RequestBody CrmContract contract) {
        contractService.save(contract);
        return R.ok();
    }

    @Operation(summary = "合同详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:contract:query')")
    public R<CrmContract> get(@PathVariable Long id) {
        return R.ok(contractService.selectWithItems(id));
    }

    @Operation(summary = "更新合同")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:contract:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新合同")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmContract contract) {
        contract.setId(id);
        contractService.updateById(contract);
        return R.ok();
    }

    @Operation(summary = "删除合同")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:contract:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除合同")
    public R<Void> delete(@PathVariable Long id) {
        contractService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "合同签署")
    @PostMapping("/{id}/sign")
    @PreAuthorize("hasAuthority('sales:contract:edit')")
    @OperationLog(module = "sales", action = "update", description = "合同签署")
    public R<Void> sign(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        contractService.sign(id, params);
        return R.ok();
    }

    @Operation(summary = "合同作废")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('sales:contract:edit')")
    @OperationLog(module = "sales", action = "update", description = "合同作废")
    public R<Void> cancel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        contractService.cancel(id, params);
        return R.ok();
    }

    @Operation(summary = "合同续签")
    @PostMapping("/{id}/renewal")
    @PreAuthorize("hasAuthority('sales:contract:edit')")
    @OperationLog(module = "sales", action = "create", description = "合同续签")
    public R<Long> renewal(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return R.ok(contractService.renewal(id, params));
    }

    /* ======== 合同附件 ======== */

    @Operation(summary = "上传合同附件")
    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasAuthority('sales:contract:edit')")
    public R<SysFile> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        SysFile sysFile = sysFileService.uploadFile(file, "contract", false);
        sysFile.setBizId(id);
        sysFileService.updateById(sysFile);
        return R.ok(sysFile);
    }

    @Operation(summary = "获取合同附件列表")
    @GetMapping("/{id}/attachments")
    @PreAuthorize("hasAuthority('sales:contract:list')")
    public R<List<SysFile>> listAttachments(@PathVariable Long id) {
        return R.ok(sysFileService.selectByBiz("contract", id));
    }

    @Operation(summary = "删除合同附件")
    @DeleteMapping("/attachments/{fileId}")
    @PreAuthorize("hasAuthority('sales:contract:edit')")
    @OperationLog(module = "sales", action = "delete", description = "删除合同附件")
    public R<Void> deleteAttachment(@PathVariable Long fileId) {
        sysFileService.removeById(fileId);
        return R.ok();
    }
}
