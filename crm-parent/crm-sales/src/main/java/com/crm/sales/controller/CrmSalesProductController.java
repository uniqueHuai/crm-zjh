package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmSalesProduct;
import com.crm.sales.service.ICrmSalesProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "产品库", description = "报价基础数据产品 CRUD")
@RestController
@RequestMapping("/sales-products")
@RequiredArgsConstructor
public class CrmSalesProductController {

    private final ICrmSalesProductService productService;

    @Operation(summary = "产品分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:product:list')")
    public R<IPage<CrmSalesProduct>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "20") long size,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String keywords,
                                           @RequestParam(required = false) Integer status) {
        return R.ok(productService.selectPageWithCondition(new Page<>(page, size), categoryId, keywords, status));
    }

    @Operation(summary = "新增产品")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:product:create')")
    @OperationLog(module = "sales", action = "create", description = "新增产品")
    public R<Void> create(@RequestBody CrmSalesProduct product) {
        productService.save(product);
        return R.ok();
    }

    @Operation(summary = "更新产品")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:product:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新产品")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmSalesProduct product) {
        product.setId(id);
        productService.updateById(product);
        return R.ok();
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:product:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除产品")
    public R<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return R.ok();
    }
}
