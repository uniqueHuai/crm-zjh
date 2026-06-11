package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.sales.entity.CrmFollowUp;
import com.crm.sales.mapper.CrmFollowUpMapper;
import com.crm.sales.service.ICrmFollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmFollowUpServiceImpl extends ServiceImpl<CrmFollowUpMapper, CrmFollowUp> implements ICrmFollowUpService {

    private final CrmFollowUpMapper followUpMapper;

    @Override
    public IPage<CrmFollowUp> selectPageWithCondition(Page<CrmFollowUp> page, Long customerId, Long opportunityId,
                                                       String type, Long creatorId, String startDate, String endDate) {
        return followUpMapper.selectPage(page, new LambdaQueryWrapper<CrmFollowUp>()
                .eq(customerId != null, CrmFollowUp::getCustomerId, customerId)
                .eq(opportunityId != null, CrmFollowUp::getOpportunityId, opportunityId)
                .eq(type != null, CrmFollowUp::getType, type)
                .eq(creatorId != null, CrmFollowUp::getCreatorId, creatorId)
                .ge(startDate != null, CrmFollowUp::getCreatedAt, startDate)
                .le(endDate != null, CrmFollowUp::getCreatedAt, endDate + " 23:59:59")
                .orderByDesc(CrmFollowUp::getCreatedAt));
    }
}
