package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.entity.SysOperationLog;
import com.crm.system.mapper.SysOperationLogMapper;
import com.crm.system.service.ISysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements ISysOperationLogService {

    private final SysOperationLogMapper logMapper;

    @Override
    public IPage<SysOperationLog> selectPageWithCondition(Page<SysOperationLog> page, String module, String action,
                                                           Long operatorId, LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .eq(module != null, SysOperationLog::getModule, module)
                .eq(action != null, SysOperationLog::getAction, action)
                .eq(operatorId != null, SysOperationLog::getOperatorId, operatorId)
                .ge(startDate != null, SysOperationLog::getCreatedAt, startDate)
                .le(endDate != null, SysOperationLog::getCreatedAt, endDate)
                .orderByDesc(SysOperationLog::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanLogs(int beforeDays) {
        LocalDateTime deadline = LocalDateTime.now().minusDays(beforeDays);
        return logMapper.delete(new LambdaQueryWrapper<SysOperationLog>()
                .lt(SysOperationLog::getCreatedAt, deadline)) >= 0;
    }
}
