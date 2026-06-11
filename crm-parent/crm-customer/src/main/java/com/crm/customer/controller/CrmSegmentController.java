package com.crm.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmCustomer;
import com.crm.customer.entity.CrmSegment;
import com.crm.customer.service.ICrmSegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "客户分群", description = "分群 CRUD、成员刷新/管理、营销活动创建")
@RestController
@RequestMapping("/segments")
@RequiredArgsConstructor
public class CrmSegmentController {

    private final ICrmSegmentService segmentService;

    @Operation(summary = "分群列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:segment:list')")
    public R<IPage<CrmSegment>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) String keywords,
                                      @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<CrmSegment> wrapper = new LambdaQueryWrapper<CrmSegment>()
                .like(keywords != null, CrmSegment::getName, keywords)
                .eq(status != null, CrmSegment::getStatus, status)
                .orderByDesc(CrmSegment::getCreatedAt);
        return R.ok(segmentService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "新增分群")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:segment:create')")
    @OperationLog(module = "customer", action = "create", description = "新增客户分群")
    public R<Void> create(@RequestBody CrmSegment segment) {
        segmentService.save(segment);
        return R.ok();
    }

    @Operation(summary = "更新分群")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:segment:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新客户分群")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmSegment segment) {
        segment.setId(id);
        segmentService.updateById(segment);
        return R.ok();
    }

    @Operation(summary = "删除分群")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:segment:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除客户分群")
    public R<Void> delete(@PathVariable Long id) {
        segmentService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "刷新分群成员")
    @PostMapping("/{id}/refresh")
    @PreAuthorize("hasAuthority('customer:segment:edit')")
    @OperationLog(module = "customer", action = "execute", description = "刷新分群成员")
    public R<Void> refresh(@PathVariable Long id) {
        segmentService.refreshMembers(id);
        return R.ok();
    }

    @Operation(summary = "获取分群成员")
    @GetMapping("/{id}/members")
    @PreAuthorize("hasAuthority('customer:segment:query')")
    public R<IPage<CrmCustomer>> members(@PathVariable Long id,
                                          @RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size) {
        return R.ok(segmentService.selectMembers(new Page<>(page, size), id));
    }

    @Operation(summary = "手动增减分群成员")
    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('customer:segment:edit')")
    @OperationLog(module = "customer", action = "update", description = "手动增减分群成员")
    public R<Void> adjustMembers(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> customerIds = ((List<Integer>) body.get("customerIds")).stream().map(Long::valueOf).toList();
        String action = (String) body.get("action");
        segmentService.manuallyAdjustMembers(id, customerIds, action);
        return R.ok();
    }

    @Operation(summary = "分群创建营销活动")
    @PostMapping("/{id}/campaign")
    @PreAuthorize("hasAuthority('customer:segment:campaign')")
    @OperationLog(module = "customer", action = "create", description = "分群创建营销活动")
    public R<Void> createCampaign(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        segmentService.createCampaign(id, params);
        return R.ok();
    }
}
