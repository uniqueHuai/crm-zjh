package com.crm.collaboration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.collaboration.entity.CrmApprovalInstance;
import com.crm.collaboration.entity.CrmApprovalRecord;

import java.util.List;

public interface ICrmApprovalInstanceService extends IService<CrmApprovalInstance> {

    IPage<CrmApprovalInstance> selectPageWithCondition(Page<CrmApprovalInstance> page, String status,
                                                        String bizType, Long applicantId,
                                                        String keywords, String dateFrom, String dateTo);

    IPage<CrmApprovalInstance> selectPending(Page<CrmApprovalInstance> page, Long approverId);

    CrmApprovalInstance selectWithRecords(Long id);

    boolean approve(Long id, Long approverId, String comment, String signatureImage);

    boolean reject(Long id, Long approverId, String comment);

    boolean recall(Long id);
}
