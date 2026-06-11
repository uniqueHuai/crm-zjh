package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmProduct;
import com.crm.mall.entity.CrmProductSku;
import com.crm.mall.service.ICrmProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品管理", description = "商品 CRUD、SKU 管理、上下架")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class CrmProductController {

    private final ICrmProductService productService;

    @Operation(summary = "商品分页列表（管理端）")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:product:list')")
    public R<IPage<CrmProduct>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) String keywords,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) Integer status) {
        return R.ok(productService.selectPageWithCondition(new Page<>(page, size), keywords, categoryId, status));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:product:create')")
    @OperationLog(module = "mall", action = "create", description = "新增商品")
    public R<Void> create(@RequestBody CrmProduct product) {
        productService.save(product);
        return R.ok();
    }

    @Operation(summary = "商品详情（含SKU）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:product:query')")
    public R<CrmProduct> get(@PathVariable Long id) {
        return R.ok(productService.selectWithSkus(id));
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新商品")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmProduct product) {
        product.setId(id);
        productService.updateById(product);
        return R.ok();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:product:delete')")
    @OperationLog(module = "mall", action = "delete", description = "删除商品")
    public R<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "上架/下架")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    @OperationLog(module = "mall", action = "update", description = "上架/下架商品")
    public R<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        productService.updateStatus(id, body.get("status"));
        return R.ok();
    }

    @Operation(summary = "批量导入商品")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('mall:product:import')")
    @OperationLog(module = "mall", action = "import", description = "导入商品")
    public R<Map<String, Object>> importProducts() {
        // TODO: implement Excel import
        return R.ok(Map.of("totalCount", 0, "successCount", 0, "failCount", 0));
    }

    @Operation(summary = "导出商品")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('mall:product:export')")
    @OperationLog(module = "mall", action = "export", description = "导出商品")
    public R<Void> export() {
        // TODO: implement async export
        return R.ok();
    }

    @Operation(summary = "新增SKU")
    @PostMapping("/{id}/skus")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    @OperationLog(module = "mall", action = "create", description = "新增SKU")
    public R<Void> createSku(@PathVariable Long id, @RequestBody CrmProductSku sku) {
        sku.setProductId(id);
        productService.createSku(sku);
        return R.ok();
    }

    @Operation(summary = "更新SKU")
    @PutMapping("/skus/{skuId}")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新SKU")
    public R<Void> updateSku(@PathVariable Long skuId, @RequestBody CrmProductSku sku) {
        sku.setId(skuId);
        productService.updateSku(sku);
        return R.ok();
    }

    @Operation(summary = "删除SKU")
    @DeleteMapping("/skus/{skuId}")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    @OperationLog(module = "mall", action = "delete", description = "删除SKU")
    public R<Void> deleteSku(@PathVariable Long skuId) {
        productService.deleteSku(skuId);
        return R.ok();
    }

    @Operation(summary = "更新SKU库存")
    @PutMapping("/skus/{skuId}/stock")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新SKU库存")
    public R<Void> updateStock(@PathVariable Long skuId, @RequestBody Map<String, Integer> body) {
        productService.updateStock(skuId, body.get("stock"));
        return R.ok();
    }
}
