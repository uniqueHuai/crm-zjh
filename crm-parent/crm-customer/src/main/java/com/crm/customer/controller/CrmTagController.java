package com.crm.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmAutoTagRule;
import com.crm.customer.entity.CrmTag;
import com.crm.customer.service.ICrmTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签管理", description = "标签 CRUD、自动标签规则管理")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class CrmTagController {

    private final ICrmTagService tagService;

    @Operation(summary = "标签列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:tag:list')")
    public R<?> list(@RequestParam(required = false, defaultValue = "1") long page,
                     @RequestParam(required = false, defaultValue = "50") long size,
                     @RequestParam(required = false) String type,
                     @RequestParam(required = false) String keywords) {
        LambdaQueryWrapper<CrmTag> wrapper = new LambdaQueryWrapper<CrmTag>()
                .eq(type != null, CrmTag::getType, type)
                .like(keywords != null, CrmTag::getName, keywords)
                .orderByAsc(CrmTag::getId);
        return R.ok(tagService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "新增标签")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:tag:create')")
    @OperationLog(module = "customer", action = "create", description = "新增标签")
    public R<Void> create(@RequestBody CrmTag tag) {
        tagService.save(tag);
        return R.ok();
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:tag:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新标签")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmTag tag) {
        tag.setId(id);
        tagService.updateById(tag);
        return R.ok();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:tag:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除标签")
    public R<Void> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "获取所有标签（前端下拉用）")
    @GetMapping("/all")
    public R<List<CrmTag>> all() {
        return R.ok(tagService.lambdaQuery()
                .eq(CrmTag::getStatus, 1)
                .orderByAsc(CrmTag::getId)
                .list());
    }

    @Operation(summary = "自动标签规则列表")
    @GetMapping("/auto-rules")
    @PreAuthorize("hasAuthority('customer:tag:auto-rule')")
    public R<IPage<CrmAutoTagRule>> autoRules(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size) {
        return R.ok(tagService.selectAutoRulePage(new Page<>(page, size)));
    }

    @Operation(summary = "新增自动标签规则")
    @PostMapping("/auto-rules")
    @PreAuthorize("hasAuthority('customer:tag:auto-rule')")
    @OperationLog(module = "customer", action = "create", description = "新增自动标签规则")
    public R<Void> createAutoRule(@RequestBody CrmAutoTagRule rule) {
        tagService.createAutoTagRule(rule);
        return R.ok();
    }

    @Operation(summary = "更新自动标签规则")
    @PutMapping("/auto-rules/{id}")
    @PreAuthorize("hasAuthority('customer:tag:auto-rule')")
    @OperationLog(module = "customer", action = "update", description = "更新自动标签规则")
    public R<Void> updateAutoRule(@PathVariable Long id, @RequestBody CrmAutoTagRule rule) {
        rule.setId(id);
        tagService.updateAutoTagRule(rule);
        return R.ok();
    }

    @Operation(summary = "删除自动标签规则")
    @DeleteMapping("/auto-rules/{id}")
    @PreAuthorize("hasAuthority('customer:tag:auto-rule')")
    @OperationLog(module = "customer", action = "delete", description = "删除自动标签规则")
    public R<Void> deleteAutoRule(@PathVariable Long id) {
        tagService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "手动执行规则")
    @PostMapping("/auto-rules/{id}/execute")
    @PreAuthorize("hasAuthority('customer:tag:auto-rule')")
    @OperationLog(module = "customer", action = "execute", description = "执行自动标签规则")
    public R<Void> executeAutoRule(@PathVariable Long id) {
        tagService.executeAutoRule(id);
        return R.ok();
    }
}
