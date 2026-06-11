package com.crm.sales.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmProductCategory;
import com.crm.sales.service.ICrmProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品分类", description = "产品分类树、CRUD")
@RestController
@RequestMapping("/product-categories")
@RequiredArgsConstructor
public class CrmProductCategoryController {

    private final ICrmProductCategoryService categoryService;

    @Operation(summary = "产品分类树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sales:category:list')")
    public R<List<CrmProductCategory>> tree() {
        return R.ok(categoryService.selectTree());
    }

    @Operation(summary = "新增产品分类")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:category:create')")
    @OperationLog(module = "sales", action = "create", description = "新增产品分类")
    public R<Void> create(@RequestBody CrmProductCategory category) {
        categoryService.save(category);
        return R.ok();
    }

    @Operation(summary = "更新产品分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:category:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新产品分类")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmProductCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        return R.ok();
    }

    @Operation(summary = "删除产品分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:category:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除产品分类")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
