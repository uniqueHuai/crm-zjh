package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmPageTemplate;
import com.crm.mall.service.ICrmPageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小程序页面管理", description = "小程序页面模板 CRUD、发布")
@RestController
@RequestMapping("/page-templates")
@RequiredArgsConstructor
public class CrmPageTemplateController {

    private final ICrmPageTemplateService pageTemplateService;

    @Operation(summary = "页面模板列表")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:page-template:list')")
    public R<IPage<CrmPageTemplate>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size,
                                          @RequestParam(required = false) String keywords,
                                          @RequestParam(required = false) Integer status) {
        return R.ok(pageTemplateService.selectPageWithCondition(new Page<>(page, size), keywords, status));
    }

    @Operation(summary = "创建页面模板")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:page-template:create')")
    @OperationLog(module = "mall", action = "create", description = "创建小程序页面模板")
    public R<Void> create(@RequestBody CrmPageTemplate template) {
        pageTemplateService.save(template);
        return R.ok();
    }

    @Operation(summary = "页面模板详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:page-template:query')")
    public R<CrmPageTemplate> get(@PathVariable Long id) {
        return R.ok(pageTemplateService.getById(id));
    }

    @Operation(summary = "更新页面模板")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:page-template:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新小程序页面模板")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmPageTemplate template) {
        template.setId(id);
        pageTemplateService.updateById(template);
        return R.ok();
    }

    @Operation(summary = "删除页面模板")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:page-template:delete')")
    @OperationLog(module = "mall", action = "delete", description = "删除小程序页面模板")
    public R<Void> delete(@PathVariable Long id) {
        pageTemplateService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "发布页面模板")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('mall:page-template:edit')")
    @OperationLog(module = "mall", action = "publish", description = "发布小程序页面模板")
    public R<Void> publish(@PathVariable Long id) {
        pageTemplateService.publish(id);
        return R.ok();
    }
}
