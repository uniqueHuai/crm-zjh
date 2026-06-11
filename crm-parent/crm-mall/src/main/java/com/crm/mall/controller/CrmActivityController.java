package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmActivity;
import com.crm.mall.service.ICrmActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "营销活动", description = "活动 CRUD、状态变更")
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class CrmActivityController {

    private final ICrmActivityService activityService;

    @Operation(summary = "活动分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:activity:list')")
    public R<IPage<CrmActivity>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) String keywords,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(activityService.selectPageWithCondition(new Page<>(page, size), keywords, type, status));
    }

    @Operation(summary = "创建活动")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:activity:create')")
    @OperationLog(module = "mall", action = "create", description = "创建营销活动")
    public R<Void> create(@RequestBody CrmActivity activity) {
        activityService.save(activity);
        return R.ok();
    }

    @Operation(summary = "活动详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:activity:query')")
    public R<CrmActivity> get(@PathVariable Long id) {
        return R.ok(activityService.getById(id));
    }

    @Operation(summary = "更新活动")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:activity:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新营销活动")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmActivity activity) {
        activity.setId(id);
        activityService.updateById(activity);
        return R.ok();
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:activity:delete')")
    @OperationLog(module = "mall", action = "delete", description = "删除营销活动")
    public R<Void> delete(@PathVariable Long id) {
        activityService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "更新活动状态（发布/结束）")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('mall:activity:edit')")
    @OperationLog(module = "mall", action = "update", description = "变更营销活动状态")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        activityService.updateStatus(id, body.get("status"));
        return R.ok();
    }
}
