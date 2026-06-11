package com.crm.sales.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmContractTemplate;
import com.crm.sales.service.ICrmContractTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "合同模板", description = "合同模板列表、CRUD")
@RestController
@RequestMapping("/contract-templates")
@RequiredArgsConstructor
public class CrmContractTemplateController {

    private final ICrmContractTemplateService templateService;

    @Operation(summary = "合同模板列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:template:list')")
    public R<List<CrmContractTemplate>> list() {
        return R.ok(templateService.lambdaQuery()
                .eq(CrmContractTemplate::getStatus, 1)
                .list());
    }

    @Operation(summary = "新增合同模板")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:template:create')")
    @OperationLog(module = "sales", action = "create", description = "新增合同模板")
    public R<Void> create(@RequestBody CrmContractTemplate template) {
        templateService.save(template);
        return R.ok();
    }

    @Operation(summary = "更新合同模板")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:template:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新合同模板")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmContractTemplate template) {
        template.setId(id);
        templateService.updateById(template);
        return R.ok();
    }

    @Operation(summary = "删除合同模板")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:template:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除合同模板")
    public R<Void> delete(@PathVariable Long id) {
        templateService.removeById(id);
        return R.ok();
    }
}
