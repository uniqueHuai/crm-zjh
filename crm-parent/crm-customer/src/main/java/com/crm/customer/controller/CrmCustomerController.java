package com.crm.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmActivityLog;
import com.crm.customer.entity.CrmContact;
import com.crm.customer.entity.CrmCustomer;
import com.crm.customer.mapper.CrmActivityLogMapper;
import com.crm.customer.service.ICrmContactService;
import com.crm.customer.service.ICrmCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "客户管理", description = "客户 CRUD、360°视图、批量标签、负责人变更、活动日志")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CrmCustomerController {

    private final ICrmCustomerService customerService;
    private final ICrmContactService contactService;
    private final CrmActivityLogMapper activityLogMapper;

    @Operation(summary = "客户分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:customer:list')")
    public R<IPage<CrmCustomer>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) String keywords,
                                       @RequestParam(required = false) Long levelId,
                                       @RequestParam(required = false) Long ownerId,
                                       @RequestParam(required = false) String sourceChannel,
                                       @RequestParam(required = false) String province,
                                       @RequestParam(required = false) String city,
                                       @RequestParam(required = false) Boolean isSleeping,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) String tagIds) {
        return R.ok(customerService.selectPageWithCondition(new Page<>(page, size), keywords, levelId,
                ownerId, sourceChannel, province, city, isSleeping, startDate, endDate, tagIds));
    }

    @Operation(summary = "新增客户")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:customer:create')")
    @OperationLog(module = "customer", action = "create", description = "新增客户")
    public R<Void> create(@RequestBody CrmCustomer customer) {
        customerService.save(customer);
        return R.ok();
    }

    @Operation(summary = "客户详情/360°视图")
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('customer:customer:query')")
    public R<CrmCustomer> detail(@PathVariable Long id) {
        return R.ok(customerService.selectDetail(id));
    }

    @Operation(summary = "更新客户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:customer:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新客户")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmCustomer customer) {
        customer.setId(id);
        customerService.updateById(customer);
        return R.ok();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:customer:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除客户")
    public R<Void> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "客户导入")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('customer:customer:import')")
    @OperationLog(module = "customer", action = "import", description = "导入客户")
    public R<Map<String, Object>> importCustomers() {
        // TODO: implement Excel import
        return R.ok(Map.of("totalCount", 0, "successCount", 0, "failCount", 0));
    }

    @Operation(summary = "客户导出")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('customer:customer:export')")
    @OperationLog(module = "customer", action = "export", description = "导出客户")
    public R<Void> export() {
        // TODO: implement async export
        return R.ok();
    }

    @Operation(summary = "批量打标签")
    @PostMapping("/batch-tag")
    @PreAuthorize("hasAuthority('customer:customer:edit')")
    @OperationLog(module = "customer", action = "update", description = "批量打标签")
    public R<Void> batchTag(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> customerIds = ((List<Integer>) body.get("customerIds")).stream().map(Long::valueOf).toList();
        @SuppressWarnings("unchecked")
        List<Long> tagIds = ((List<Integer>) body.get("tagIds")).stream().map(Long::valueOf).toList();
        String mode = (String) body.getOrDefault("mode", "append");
        customerService.batchTag(customerIds, tagIds, mode);
        return R.ok();
    }

    @Operation(summary = "批量移除标签")
    @DeleteMapping("/batch-tag")
    @PreAuthorize("hasAuthority('customer:customer:edit')")
    @OperationLog(module = "customer", action = "update", description = "批量移除标签")
    public R<Void> batchRemoveTag(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> customerIds = ((List<Integer>) body.get("customerIds")).stream().map(Long::valueOf).toList();
        @SuppressWarnings("unchecked")
        List<Long> tagIds = ((List<Integer>) body.get("tagIds")).stream().map(Long::valueOf).toList();
        customerService.batchRemoveTag(customerIds, tagIds);
        return R.ok();
    }

    @Operation(summary = "变更负责人")
    @PutMapping("/{id}/transfer")
    @PreAuthorize("hasAuthority('customer:customer:edit')")
    @OperationLog(module = "customer", action = "update", description = "变更负责人")
    public R<Void> transfer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long newOwnerId = Long.valueOf(body.get("newOwnerId").toString());
        Boolean transferFollowUps = (Boolean) body.getOrDefault("transferFollowUps", false);
        customerService.transferOwner(id, newOwnerId, transferFollowUps);
        return R.ok();
    }

    @Operation(summary = "批量调整客户等级")
    @PutMapping("/batch-level")
    @PreAuthorize("hasAuthority('customer:customer:edit')")
    @OperationLog(module = "customer", action = "update", description = "批量调整等级")
    public R<Void> batchLevel(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> customerIds = ((List<Integer>) body.get("customerIds")).stream().map(Long::valueOf).toList();
        Long levelId = Long.valueOf(body.get("levelId").toString());
        String reason = (String) body.getOrDefault("reason", "");
        customerService.batchChangeLevel(customerIds, levelId, reason);
        return R.ok();
    }

    @Operation(summary = "客户操作日志")
    @GetMapping("/{id}/activity-logs")
    @PreAuthorize("hasAuthority('customer:customer:query')")
    public R<IPage<CrmActivityLog>> activityLogs(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size) {
        return R.ok(activityLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<CrmActivityLog>()
                        .eq(CrmActivityLog::getCustomerId, id)
                        .orderByDesc(CrmActivityLog::getCreatedAt)));
    }

    @Operation(summary = "客户下联系人列表")
    @GetMapping("/{customerId}/contacts")
    @PreAuthorize("hasAuthority('customer:customer:query')")
    public R<List<CrmContact>> contacts(@PathVariable Long customerId) {
        return R.ok(contactService.selectByCustomerId(customerId));
    }
}
