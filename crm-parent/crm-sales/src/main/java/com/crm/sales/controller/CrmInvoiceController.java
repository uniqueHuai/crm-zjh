package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmInvoice;
import com.crm.sales.service.ICrmInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "发票管理", description = "发票列表、开票、邮寄、签收、作废")
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class CrmInvoiceController {

    private final ICrmInvoiceService invoiceService;

    @Operation(summary = "发票分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:invoice:list')")
    public R<IPage<CrmInvoice>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) Long customerId,
                                      @RequestParam(required = false) Long contractId,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate) {
        return R.ok(invoiceService.selectPageWithCondition(new Page<>(page, size), customerId,
                contractId, status, startDate, endDate));
    }

    @Operation(summary = "开票申请")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:invoice:create')")
    @OperationLog(module = "sales", action = "create", description = "开票申请")
    public R<Void> create(@RequestBody CrmInvoice invoice) {
        invoiceService.save(invoice);
        return R.ok();
    }

    @Operation(summary = "开票处理")
    @PutMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('sales:invoice:issue')")
    @OperationLog(module = "sales", action = "update", description = "开票处理")
    public R<Void> issue(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        invoiceService.issue(id, params);
        return R.ok();
    }

    @Operation(summary = "发票邮寄")
    @PutMapping("/{id}/ship")
    @PreAuthorize("hasAuthority('sales:invoice:ship')")
    @OperationLog(module = "sales", action = "update", description = "发票邮寄")
    public R<Void> ship(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        invoiceService.ship(id, params);
        return R.ok();
    }

    @Operation(summary = "发票签收确认")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('sales:invoice:confirm')")
    @OperationLog(module = "sales", action = "update", description = "发票签收确认")
    public R<Void> confirm(@PathVariable Long id) {
        invoiceService.confirm(id);
        return R.ok();
    }

    @Operation(summary = "发票作废")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('sales:invoice:cancel')")
    @OperationLog(module = "sales", action = "update", description = "发票作废")
    public R<Void> cancel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        invoiceService.cancelInvoice(id, params);
        return R.ok();
    }

    @Operation(summary = "发票详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:invoice:query')")
    public R<CrmInvoice> get(@PathVariable Long id) {
        return R.ok(invoiceService.getById(id));
    }
}
