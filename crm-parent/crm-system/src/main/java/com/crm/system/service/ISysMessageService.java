package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysMessage;

import java.util.Map;

public interface ISysMessageService extends IService<SysMessage> {

    IPage<SysMessage> selectPage(Page<SysMessage> page, Long receiverId, Boolean isRead,
                                 String keywords, String bizType, String dateFrom, String dateTo);

    boolean markAsRead(Long id);

    boolean markAllAsRead(Long receiverId);

    boolean sendMessage(SysMessage message);

    Map<String, Object> unreadCount(Long receiverId);
}
