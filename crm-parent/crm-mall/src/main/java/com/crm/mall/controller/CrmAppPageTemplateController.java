package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.model.R;
import com.crm.mall.entity.CrmPageTemplate;
import com.crm.mall.service.ICrmPageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小程序端页面模板")
@RestController
@RequestMapping("/app/page-templates")
@RequiredArgsConstructor
public class CrmAppPageTemplateController {

    private final ICrmPageTemplateService pageTemplateService;

    @Operation(summary = "获取已发布的页面模板列表")
    @GetMapping
    public R<List<CrmPageTemplate>> list(@RequestParam(required = false) String pageType) {
        LambdaQueryWrapper<CrmPageTemplate> qw = new LambdaQueryWrapper<CrmPageTemplate>()
                .eq(CrmPageTemplate::getStatus, 1)
                .eq(pageType != null && !pageType.isEmpty(), CrmPageTemplate::getPageType, pageType)
                .orderByDesc(CrmPageTemplate::getUpdatedAt);
        return R.ok(pageTemplateService.list(qw));
    }

    @Operation(summary = "获取指定类型的已发布页面模板详情")
    @GetMapping("/{pageType}")
    public R<CrmPageTemplate> getByType(@PathVariable String pageType) {
        CrmPageTemplate template = pageTemplateService.getOne(
                new LambdaQueryWrapper<CrmPageTemplate>()
                        .eq(CrmPageTemplate::getPageType, pageType)
                        .eq(CrmPageTemplate::getStatus, 1)
                        .last("LIMIT 1"));
        return template != null ? R.ok(template) : R.failed(404, "未找到已发布的页面模板");
    }
}
