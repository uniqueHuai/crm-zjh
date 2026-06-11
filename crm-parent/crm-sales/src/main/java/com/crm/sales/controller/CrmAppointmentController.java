package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmAppointment;
import com.crm.sales.service.ICrmAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "拜访计划与日程", description = "日程 CRUD、签到、完成、日历视图")
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class CrmAppointmentController {

    private final ICrmAppointmentService appointmentService;

    @Operation(summary = "日程列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:appointment:list')")
    public R<IPage<CrmAppointment>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) Long ownerId) {
        return R.ok(appointmentService.selectPageWithCondition(new Page<>(page, size), startDate, endDate, status, ownerId));
    }

    @Operation(summary = "新增拜访计划")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:appointment:create')")
    @OperationLog(module = "sales", action = "create", description = "新增拜访计划")
    public R<Void> create(@RequestBody CrmAppointment appointment) {
        appointmentService.save(appointment);
        return R.ok();
    }

    @Operation(summary = "更新拜访计划")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:appointment:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新拜访计划")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmAppointment appointment) {
        appointment.setId(id);
        appointmentService.updateById(appointment);
        return R.ok();
    }

    @Operation(summary = "删除拜访计划")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:appointment:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除拜访计划")
    public R<Void> delete(@PathVariable Long id) {
        appointmentService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "签到")
    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasAuthority('sales:appointment:edit')")
    @OperationLog(module = "sales", action = "update", description = "签到")
    public R<Void> checkIn(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        appointmentService.checkIn(id, params);
        return R.ok();
    }

    @Operation(summary = "完成拜访")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('sales:appointment:edit')")
    @OperationLog(module = "sales", action = "update", description = "完成拜访")
    public R<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        appointmentService.complete(id, params);
        return R.ok();
    }

    @Operation(summary = "日历视图")
    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('sales:appointment:list')")
    public R<List<Map<String, Object>>> calendar(@RequestParam String startDate,
                                                  @RequestParam String endDate,
                                                  @RequestParam(required = false) Long ownerId) {
        return R.ok(appointmentService.calendar(startDate, endDate, ownerId));
    }

    @Operation(summary = "设置完成状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sales:appointment:edit')")
    @OperationLog(module = "sales", action = "update", description = "设置日程状态")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        appointmentService.lambdaUpdate().eq(CrmAppointment::getId, id)
                .set(CrmAppointment::getStatus, body.get("status"))
                .update();
        return R.ok();
    }
}
