package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.sales.entity.CrmQuotation;
import com.crm.sales.entity.CrmQuotationItem;
import com.crm.sales.mapper.CrmQuotationItemMapper;
import com.crm.sales.mapper.CrmQuotationMapper;
import com.crm.sales.service.ICrmQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrmQuotationServiceImpl extends ServiceImpl<CrmQuotationMapper, CrmQuotation>
        implements ICrmQuotationService {

    private final CrmQuotationMapper quotationMapper;
    private final CrmQuotationItemMapper quotationItemMapper;

    @Override
    public IPage<CrmQuotation> selectPageWithCondition(Page<CrmQuotation> page, String keywords, Long customerId, Long opportunityId,
                                                        String status, String startDate, String endDate) {
        return quotationMapper.selectPageWithCondition(page, keywords, customerId, opportunityId, status, startDate, endDate);
    }

    @Override
    public CrmQuotation selectWithItems(Long id) {
        CrmQuotation quotation = getById(id);
        if (quotation == null) throw new BizException(404, "报价单不存在");
        quotation.setItems(quotationItemMapper.selectList(
                new LambdaQueryWrapper<CrmQuotationItem>()
                        .eq(CrmQuotationItem::getQuotationId, id)));
        return quotation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApproval(Long id, String remark) {
        return lambdaUpdate().eq(CrmQuotation::getId, id)
                .set(CrmQuotation::getStatus, "pending_approval")
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, String comment) {
        return lambdaUpdate().eq(CrmQuotation::getId, id)
                .set(CrmQuotation::getStatus, "approved")
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long id, String comment) {
        return lambdaUpdate().eq(CrmQuotation::getId, id)
                .set(CrmQuotation::getStatus, "rejected")
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean voidQuotation(Long id, String reason) {
        return lambdaUpdate().eq(CrmQuotation::getId, id)
                .set(CrmQuotation::getStatus, "void")
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateContract(Long id) {
        // TODO: implement contract generation from quotation
        return null;
    }
}
