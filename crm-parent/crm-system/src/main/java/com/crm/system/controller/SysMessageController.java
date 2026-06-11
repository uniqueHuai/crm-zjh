package com.crm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.security.SecurityUtils;
import com.crm.system.entity.SysMessage;
import com.crm.system.service.ISysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "消息中心", description = "站内消息、未读计数")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class SysMessageController {

    private final ISysMessageService messageService;

    @Operation(summary = "消息分页列表")
    @GetMapping
    public R<IPage<SysMessage>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) Boolean isRead,
                                      @RequestParam(required = false) String keywords,
                                      @RequestParam(required = false) String bizType,
                                      @RequestParam(required = false) String dateFrom,
                                      @RequestParam(required = false) String dateTo) {
        Long receiverId = SecurityUtils.getUserId();
        return R.ok(messageService.selectPage(new Page<>(page, size), receiverId, isRead, keywords, bizType, dateFrom, dateTo));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return R.ok();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        Long receiverId = SecurityUtils.getUserId();
        messageService.markAllAsRead(receiverId);
        return R.ok();
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('system:message:send')")
    public R<Void> send(@RequestBody SysMessage message) {
        messageService.sendMessage(message);
        return R.ok();
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public R<Map<String, Object>> unreadCount() {
        Long receiverId = SecurityUtils.getUserId();
        return R.ok(messageService.unreadCount(receiverId));
    }
}
