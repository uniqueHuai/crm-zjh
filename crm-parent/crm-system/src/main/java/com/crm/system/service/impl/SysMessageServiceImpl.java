package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.system.entity.SysMessage;
import com.crm.system.entity.SysUser;
import com.crm.system.mapper.SysMessageMapper;
import com.crm.system.mapper.SysUserMapper;
import com.crm.system.service.ISysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

    private final SysUserMapper userMapper;

    @Override
    public IPage<SysMessage> selectPage(Page<SysMessage> page, Long receiverId, Boolean isRead,
                                        String keywords, String bizType, String dateFrom, String dateTo) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(isRead != null, SysMessage::getIsRead, isRead)
                .like(keywords != null && !keywords.isEmpty(), SysMessage::getTitle, keywords)
                .eq(bizType != null && !bizType.isEmpty(), SysMessage::getBizType, bizType)
                .ge(dateFrom != null && !dateFrom.isEmpty(), SysMessage::getCreatedAt, dateFrom)
                .le(dateTo != null && !dateTo.isEmpty(), SysMessage::getCreatedAt, dateTo + " 23:59:59")
                .orderByDesc(SysMessage::getCreatedAt);
        IPage<SysMessage> result = page(page, wrapper);

        // populate creator names from sys_user
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            Set<Long> userIds = result.getRecords().stream()
                    .map(SysMessage::getCreatedBy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!userIds.isEmpty()) {
                Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, Function.identity()));
                for (SysMessage msg : result.getRecords()) {
                    SysUser user = userMap.get(msg.getCreatedBy());
                    msg.setCreatorName(user != null ? user.getRealName() : "系统");
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id) {
        return lambdaUpdate()
                .eq(SysMessage::getId, id)
                .set(SysMessage::getIsRead, true)
                .set(SysMessage::getReadAt, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long receiverId) {
        return lambdaUpdate()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(SysMessage::getIsRead, false)
                .set(SysMessage::getIsRead, true)
                .set(SysMessage::getReadAt, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendMessage(SysMessage message) {
        if (message.getReceiverIds() != null && !message.getReceiverIds().isEmpty()) {
            for (Long receiverId : message.getReceiverIds()) {
                SysMessage msg = new SysMessage();
                msg.setReceiverId(receiverId);
                msg.setChannel(message.getChannel());
                msg.setTitle(message.getTitle());
                msg.setContent(message.getContent());
                msg.setBizType(message.getBizType());
                msg.setBizId(message.getBizId());
                msg.setPriority(message.getPriority());
                msg.setIsRead(false);
                save(msg);
            }
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> unreadCount(Long receiverId) {
        long total = lambdaQuery()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(SysMessage::getIsRead, false)
                .count();
        List<SysMessage> unreadList = lambdaQuery()
                .eq(SysMessage::getReceiverId, receiverId)
                .eq(SysMessage::getIsRead, false)
                .select(SysMessage::getBizType)
                .list();
        Map<String, Long> byType = new HashMap<>();
        unreadList.forEach(m -> byType.merge(m.getBizType(), 1L, Long::sum));
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("byType", byType);
        return result;
    }
}
