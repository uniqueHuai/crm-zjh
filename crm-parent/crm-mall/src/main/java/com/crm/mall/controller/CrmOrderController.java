package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmOrder;
import com.crm.mall.service.ICrmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单管理", description = "订单列表、详情、状态变更")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class CrmOrderController {

    private final ICrmOrderService orderService;

    @Operation(summary = "订单分页列表（管理端）")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:order:list')")
    public R<IPage<CrmOrder>> page(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "20") long size,
                                    @RequestParam(required = false) Long customerId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @RequestParam(required = false) String keywords) {
        return R.ok(orderService.selectPageWithCondition(new Page<>(page, size), customerId, status, startDate, endDate, keywords));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:order:query')")
    public R<CrmOrder> get(@PathVariable Long id) {
        return R.ok(orderService.selectWithItems(id));
    }

    @Operation(summary = "订单状态变更（发货/完成/取消）")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('mall:order:edit')")
    @OperationLog(module = "mall", action = "update", description = "订单状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateStatus(id, null, body.get("status"));
        return R.ok();
    }
}
