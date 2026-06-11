package com.crm.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.enums.ResultCode;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmLead;
import com.crm.customer.service.ICrmLeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "线索管理", description = "线索 CRUD、导入、转换、分配、查重合并、回池等")
@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class CrmLeadController {

    private final ICrmLeadService leadService;

    @Operation(summary = "线索分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:lead:list')")
    public R<IPage<CrmLead>> page(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "20") long size,
                                   @RequestParam(required = false) String keywords,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String sourceChannel,
                                   @RequestParam(required = false) Long ownerId,
                                   @RequestParam(required = false) String assignType,
                                   @RequestParam(required = false) String province,
                                   @RequestParam(required = false) String city,
                                   @RequestParam(required = false) String industry,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) Boolean poolReturn) {
        return R.ok(leadService.selectPageWithCondition(new Page<>(page, size), keywords, status,
                sourceChannel, ownerId, assignType, province, city, industry, startDate, endDate, poolReturn));
    }

    @Operation(summary = "新增线索")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:lead:create')")
    @OperationLog(module = "customer", action = "create", description = "新增线索")
    public R<Void> create(@RequestBody CrmLead lead) {
        leadService.save(lead);
        return R.ok();
    }

    @Operation(summary = "线索详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:lead:query')")
    public R<CrmLead> get(@PathVariable Long id) {
        CrmLead lead = leadService.selectWithDedupInfo(id);
        return R.ok(lead);
    }

    @Operation(summary = "更新线索")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:lead:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新线索")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmLead lead) {
        lead.setId(id);
        leadService.updateById(lead);
        return R.ok();
    }

    @Operation(summary = "删除线索")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:lead:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除线索")
    public R<Void> delete(@PathVariable Long id) {
        leadService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "线索导入")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('customer:lead:import')")
    @OperationLog(module = "customer", action = "import", description = "导入线索")
    public R<Map<String, Object>> importLeads(@RequestParam("file") MultipartFile file) {
        return R.ok(leadService.importLeads(file));
    }

    @Operation(summary = "线索转客户")
    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAuthority('customer:lead:convert')")
    @OperationLog(module = "customer", action = "update", description = "线索转客户")
    public R<Void> convert(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        leadService.convertLead(id, params);
        return R.ok();
    }

    @Operation(summary = "线索分配")
    @PostMapping("/distribute")
    @PreAuthorize("hasAuthority('customer:lead:distribute')")
    @OperationLog(module = "customer", action = "update", description = "线索分配")
    public R<Void> distribute(@RequestBody Map<String, Object> body) {
        Object rawIds = body.get("leadIds");
        if (rawIds == null) {
            return R.failed(ResultCode.INVALID_PARAM, "leadIds 不能为空");
        }
        List<Long> leadIds = new java.util.ArrayList<>();
        if (rawIds instanceof List<?> list) {
            for (Object item : list) {
                leadIds.add(Long.valueOf(item.toString()));
            }
        }
        if (leadIds.isEmpty()) {
            return R.failed(ResultCode.INVALID_PARAM, "请至少选择一条线索");
        }
        Long ownerId = body.get("ownerId") != null ? Long.valueOf(body.get("ownerId").toString()) : null;
        if (ownerId == null) {
            return R.failed(ResultCode.INVALID_PARAM, "请选择负责人");
        }
        String assignType = (String) body.get("assignType");
        leadService.distributeLeads(leadIds, ownerId, assignType);
        return R.ok();
    }

    @Operation(summary = "线索查重检测")
    @PostMapping("/dedup-check")
    @PreAuthorize("hasAuthority('customer:lead:query')")
    public R<Map<String, Object>> dedupCheck(@RequestBody Map<String, String> body) {
        return R.ok(leadService.dedupCheck(body.get("phone"), body.get("wechatUnionId")));
    }

    @Operation(summary = "线索合并")
    @PostMapping("/merge")
    @PreAuthorize("hasAuthority('customer:lead:edit')")
    @OperationLog(module = "customer", action = "update", description = "线索合并")
    public R<Void> merge(@RequestBody Map<String, Object> body) {
        Long mainLeadId = Long.valueOf(body.get("mainLeadId").toString());
        @SuppressWarnings("unchecked")
        List<Long> mergeIds = ((List<Integer>) body.get("mergeLeadIds")).stream().map(Long::valueOf).toList();
        @SuppressWarnings("unchecked")
        Map<String, String> fieldStrategy = (Map<String, String>) body.get("fieldStrategy");
        leadService.mergeLeads(mainLeadId, mergeIds, fieldStrategy);
        return R.ok();
    }

    @Operation(summary = "线索回池")
    @PutMapping("/{id}/pool-return")
    @PreAuthorize("hasAuthority('customer:lead:edit')")
    @OperationLog(module = "customer", action = "update", description = "线索回池")
    public R<Void> poolReturn(@PathVariable Long id) {
        leadService.poolReturn(id);
        return R.ok();
    }

    @Operation(summary = "线索批量操作")
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('customer:lead:edit')")
    @OperationLog(module = "customer", action = "update", description = "线索批量操作")
    public R<Void> batch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> leadIds = ((List<Integer>) body.get("leadIds")).stream().map(Long::valueOf).toList();
        String action = (String) body.get("action");
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) body.get("payload");
        leadService.batchOperate(leadIds, action, payload);
        return R.ok();
    }
}
