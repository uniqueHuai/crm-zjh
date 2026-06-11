package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmFollowUp;
import com.crm.sales.service.ICrmFollowUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "跟进记录", description = "跟进记录 CRUD、客户时间线")
@RestController
@RequestMapping("/follow-ups")
@RequiredArgsConstructor
public class CrmFollowUpController {

    private final ICrmFollowUpService followUpService;

    @Operation(summary = "跟进记录分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:followup:list')")
    public R<IPage<CrmFollowUp>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) Long customerId,
                                       @RequestParam(required = false) Long opportunityId,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) Long creatorId,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return R.ok(followUpService.selectPageWithCondition(new Page<>(page, size), customerId,
                opportunityId, type, creatorId, startDate, endDate));
    }

    @Operation(summary = "新增跟进记录")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:followup:create')")
    @OperationLog(module = "sales", action = "create", description = "新增跟进记录")
    public R<Void> create(@RequestBody CrmFollowUp followUp) {
        followUpService.save(followUp);
        return R.ok();
    }

    @Operation(summary = "跟进记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:followup:query')")
    public R<CrmFollowUp> get(@PathVariable Long id) {
        return R.ok(followUpService.getById(id));
    }

    @Operation(summary = "更新跟进记录")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:followup:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新跟进记录")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmFollowUp followUp) {
        followUp.setId(id);
        followUpService.updateById(followUp);
        return R.ok();
    }

    @Operation(summary = "删除跟进记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:followup:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除跟进记录")
    public R<Void> delete(@PathVariable Long id) {
        followUpService.removeById(id);
        return R.ok();
    }
}
