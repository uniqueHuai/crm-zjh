package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.entity.CrmCategory;
import com.crm.mall.service.ICrmCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "小程序端分类")
@RestController
@RequestMapping("/app/products")
@RequiredArgsConstructor
public class CrmAppCategoryController {

    private final ICrmCategoryService categoryService;

    @Operation(summary = "小程序端分类列表（仅启用的顶级分类）")
    @GetMapping("/categories")
    public R<List<CrmCategory>> categories() {
        return R.ok(categoryService.selectTree());
    }
}
