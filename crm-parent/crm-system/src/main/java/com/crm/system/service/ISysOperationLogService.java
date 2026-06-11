package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysOperationLog;

import java.time.LocalDateTime;

public interface ISysOperationLogService extends IService<SysOperationLog> {

    IPage<SysOperationLog> selectPageWithCondition(Page<SysOperationLog> page, String module, String action,
                                                    Long operatorId, LocalDateTime startDate, LocalDateTime endDate);

    boolean cleanLogs(int beforeDays);
}
