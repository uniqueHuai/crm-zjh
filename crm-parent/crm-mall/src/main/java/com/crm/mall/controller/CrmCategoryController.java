package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmCategory;
import com.crm.mall.service.ICrmCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品分类管理", description = "商品分类树、CRUD")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CrmCategoryController {

    private final ICrmCategoryService categoryService;

    @Operation(summary = "分类树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('mall:category:list')")
    public R<List<CrmCategory>> tree() {
        return R.ok(categoryService.selectTree());
    }

    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:category:create')")
    @OperationLog(module = "mall", action = "create", description = "新增商品分类")
    public R<Void> create(@RequestBody CrmCategory category) {
        categoryService.save(category);
        return R.ok();
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:category:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新商品分类")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        return R.ok();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:category:delete')")
    @OperationLog(module = "mall", action = "delete", description = "删除商品分类")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
