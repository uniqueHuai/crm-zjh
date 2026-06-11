package com.crm.collaboration.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.collaboration.entity.CrmTicket;
import com.crm.collaboration.service.ICrmTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "服务工单", description = "工单 CRUD、流转、评价")
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class CrmTicketController {

    private final ICrmTicketService ticketService;

    @Operation(summary = "工单分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('collaboration:ticket:list')")
    public R<IPage<CrmTicket>> list(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "20") long size,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) String priority,
                                    @RequestParam(required = false) String keywords,
                                    @RequestParam(required = false) Long assigneeId) {
        LambdaQueryWrapper<CrmTicket> wrapper = new LambdaQueryWrapper<CrmTicket>()
                .eq(status != null, CrmTicket::getStatus, status)
                .eq(type != null, CrmTicket::getType, type)
                .eq(priority != null, CrmTicket::getPriority, priority)
                .eq(assigneeId != null, CrmTicket::getAssigneeId, assigneeId)
                .and(keywords != null && !keywords.isEmpty(), w -> w
                        .like(CrmTicket::getTitle, keywords)
                        .or()
                        .like(CrmTicket::getTicketNo, keywords)
                        .or()
                        .apply("EXISTS (SELECT 1 FROM crm_customer c WHERE c.id = customer_id AND c.name LIKE {0})", "%" + keywords + "%"))
                .orderByDesc(CrmTicket::getCreatedAt);
        return R.ok(ticketService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "创建工单")
    @PostMapping
    @PreAuthorize("hasAuthority('collaboration:ticket:create')")
    @OperationLog(module = "collaboration", action = "create", description = "创建工单")
    public R<Void> create(@RequestBody CrmTicket ticket) {
        ticket.setStatus("pending");
        ticketService.save(ticket);
        return R.ok();
    }

    @Operation(summary = "工单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:ticket:query')")
    public R<CrmTicket> get(@PathVariable Long id) {
        return R.ok(ticketService.getById(id));
    }

    @Operation(summary = "更新工单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:ticket:edit')")
    @OperationLog(module = "collaboration", action = "update", description = "更新工单")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmTicket ticket) {
        ticket.setId(id);
        ticketService.updateById(ticket);
        return R.ok();
    }

    @Operation(summary = "删除工单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('collaboration:ticket:delete')")
    @OperationLog(module = "collaboration", action = "delete", description = "删除工单")
    public R<Void> delete(@PathVariable Long id) {
        ticketService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "指派处理人")
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('collaboration:ticket:assign')")
    @OperationLog(module = "collaboration", action = "update", description = "指派工单处理人")
    public R<Void> assign(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        ticketService.lambdaUpdate().eq(CrmTicket::getId, id)
                .set(CrmTicket::getAssigneeId, body.get("assigneeId"))
                .set(CrmTicket::getStatus, "assigned")
                .update();
        return R.ok();
    }

    @Operation(summary = "接单")
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('collaboration:ticket:accept')")
    @OperationLog(module = "collaboration", action = "update", description = "接单")
    public R<Void> accept(@PathVariable Long id) {
        ticketService.lambdaUpdate().eq(CrmTicket::getId, id)
                .set(CrmTicket::getStatus, "accepted")
                .update();
        return R.ok();
    }

    @Operation(summary = "开始处理")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('collaboration:ticket:start')")
    @OperationLog(module = "collaboration", action = "update", description = "开始处理工单")
    public R<Void> start(@PathVariable Long id) {
        ticketService.lambdaUpdate().eq(CrmTicket::getId, id)
                .set(CrmTicket::getStatus, "in_progress")
                .update();
        return R.ok();
    }

    @Operation(summary = "完成处理")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('collaboration:ticket:complete')")
    @OperationLog(module = "collaboration", action = "update", description = "完成工单处理")
    public R<Void> complete(@PathVariable Long id) {
        ticketService.lambdaUpdate().eq(CrmTicket::getId, id)
                .set(CrmTicket::getStatus, "completed")
                .update();
        return R.ok();
    }

    @Operation(summary = "客户评价")
    @PutMapping("/{id}/rate")
    @PreAuthorize("hasAuthority('collaboration:ticket:rate')")
    @OperationLog(module = "collaboration", action = "update", description = "客户评价工单")
    public R<Void> rate(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        ticketService.lambdaUpdate().eq(CrmTicket::getId, id)
                .set(CrmTicket::getRating, body.get("rating"))
                .set(CrmTicket::getStatus, "rated")
                .update();
        return R.ok();
    }
}
