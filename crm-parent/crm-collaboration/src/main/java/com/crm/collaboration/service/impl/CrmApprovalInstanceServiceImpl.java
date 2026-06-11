package com.crm.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.collaboration.entity.CrmApprovalInstance;
import com.crm.collaboration.entity.CrmApprovalRecord;
import com.crm.collaboration.mapper.CrmApprovalInstanceMapper;
import com.crm.collaboration.mapper.CrmApprovalRecordMapper;
import com.crm.collaboration.service.ICrmApprovalInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmApprovalInstanceServiceImpl extends ServiceImpl<CrmApprovalInstanceMapper, CrmApprovalInstance>
        implements ICrmApprovalInstanceService {

    private final CrmApprovalInstanceMapper instanceMapper;
    private final CrmApprovalRecordMapper recordMapper;

    @Override
    public IPage<CrmApprovalInstance> selectPageWithCondition(Page<CrmApprovalInstance> page, String status,
                                                               String bizType, Long applicantId,
                                                               String keywords, String dateFrom, String dateTo) {
        LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom + "T00:00:00") : null;
        LocalDateTime to = dateTo != null ? LocalDateTime.parse(dateTo + "T23:59:59") : null;

        return instanceMapper.selectPage(page, new LambdaQueryWrapper<CrmApprovalInstance>()
                .eq(status != null, CrmApprovalInstance::getStatus, status)
                .eq(bizType != null, CrmApprovalInstance::getBizType, bizType)
                .eq(applicantId != null, CrmApprovalInstance::getApplicantId, applicantId)
                .and(keywords != null && !keywords.isEmpty(), w -> w
                        .like(CrmApprovalInstance::getApplicantName, keywords)
                        .or()
                        .like(CrmApprovalInstance::getBizType, keywords)
                        .or()
                        .apply("form_data ->> 'title' LIKE {0}", "%" + keywords + "%"))
                .ge(from != null, CrmApprovalInstance::getCreatedAt, from)
                .le(to != null, CrmApprovalInstance::getCreatedAt, to)
                .orderByDesc(CrmApprovalInstance::getCreatedAt));
    }

    @Override
    public IPage<CrmApprovalInstance> selectPending(Page<CrmApprovalInstance> page, Long approverId) {
        // simplified: return instances with pending status
        return instanceMapper.selectPage(page, new LambdaQueryWrapper<CrmApprovalInstance>()
                .eq(CrmApprovalInstance::getStatus, "pending")
                .orderByDesc(CrmApprovalInstance::getCreatedAt));
    }

    @Override
    public CrmApprovalInstance selectWithRecords(Long id) {
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, Long approverId, String comment, String signatureImage) {
        CrmApprovalRecord record = new CrmApprovalRecord();
        record.setInstanceId(id);
        record.setApproverId(approverId);
        record.setAction("approve");
        record.setComment(comment);
        record.setSignatureImage(signatureImage);
        record.setActedAt(LocalDateTime.now());
        recordMapper.insert(record);

        lambdaUpdate().eq(CrmApprovalInstance::getId, id)
                .set(CrmApprovalInstance::getStatus, "approved")
                .set(CrmApprovalInstance::getCompletedAt, LocalDateTime.now())
                .update();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long id, Long approverId, String comment) {
        CrmApprovalRecord record = new CrmApprovalRecord();
        record.setInstanceId(id);
        record.setApproverId(approverId);
        record.setAction("reject");
        record.setComment(comment);
        record.setActedAt(LocalDateTime.now());
        recordMapper.insert(record);

        lambdaUpdate().eq(CrmApprovalInstance::getId, id)
                .set(CrmApprovalInstance::getStatus, "rejected")
                .set(CrmApprovalInstance::getCompletedAt, LocalDateTime.now())
                .update();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recall(Long id) {
        return lambdaUpdate().eq(CrmApprovalInstance::getId, id)
                .set(CrmApprovalInstance::getStatus, "recalled")
                .update();
    }
}
