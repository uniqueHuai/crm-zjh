package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.mall.entity.CrmProduct;
import com.crm.mall.service.ICrmProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小程序端商品")
@RestController
@RequestMapping("/app/products")
@RequiredArgsConstructor
public class CrmAppProductController {

    private final ICrmProductService productService;

    @Operation(summary = "小程序端商品列表（仅上架+库存>0）")
    @GetMapping
    public R<IPage<CrmProduct>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String keywords) {
        return R.ok(productService.selectPageWithCondition(new Page<>(page, size), keywords, categoryId, 1));
    }

    @Operation(summary = "小程序端商品详情")
    @GetMapping("/{id}")
    public R<CrmProduct> detail(@PathVariable Long id) {
        return R.ok(productService.selectWithSkus(id));
    }
}
