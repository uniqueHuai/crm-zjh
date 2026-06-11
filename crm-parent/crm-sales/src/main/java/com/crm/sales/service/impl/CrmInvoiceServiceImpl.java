package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.sales.entity.CrmInvoice;
import com.crm.sales.mapper.CrmInvoiceMapper;
import com.crm.sales.service.ICrmInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmInvoiceServiceImpl extends ServiceImpl<CrmInvoiceMapper, CrmInvoice> implements ICrmInvoiceService {

    private final CrmInvoiceMapper invoiceMapper;

    @Override
    public IPage<CrmInvoice> selectPageWithCondition(Page<CrmInvoice> page, Long customerId, Long contractId,
                                                      String status, String startDate, String endDate) {
        return invoiceMapper.selectPage(page, new LambdaQueryWrapper<CrmInvoice>()
                .eq(customerId != null, CrmInvoice::getCustomerId, customerId)
                .eq(contractId != null, CrmInvoice::getContractId, contractId)
                .eq(status != null, CrmInvoice::getStatus, status)
                .ge(startDate != null, CrmInvoice::getCreatedAt, startDate)
                .le(endDate != null, CrmInvoice::getCreatedAt, endDate + " 23:59:59")
                .orderByDesc(CrmInvoice::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean issue(Long id, Map<String, Object> params) {
        CrmInvoice invoice = getById(id);
        if (invoice == null) throw new BizException(404, "发票不存在");
        return lambdaUpdate().eq(CrmInvoice::getId, id)
                .set(CrmInvoice::getStatus, "issued")
                .set(CrmInvoice::getInvoiceNo, params.get("invoiceNo"))
                .set(CrmInvoice::getInvoiceFileUrl, params.get("invoiceFileUrl"))
                .set(CrmInvoice::getIssueDate, params.get("issueDate"))
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ship(Long id, Map<String, Object> params) {
        return lambdaUpdate().eq(CrmInvoice::getId, id)
                .set(CrmInvoice::getStatus, "shipped")
                .set(CrmInvoice::getExpressCompany, params.get("expressCompany"))
                .set(CrmInvoice::getExpressNo, params.get("expressNo"))
                .set(CrmInvoice::getShipDate, params.get("shipDate"))
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(Long id) {
        return lambdaUpdate().eq(CrmInvoice::getId, id)
                .set(CrmInvoice::getStatus, "confirmed")
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelInvoice(Long id, Map<String, Object> params) {
        return lambdaUpdate().eq(CrmInvoice::getId, id)
                .set(CrmInvoice::getStatus, "cancelled")
                .update();
    }
}
