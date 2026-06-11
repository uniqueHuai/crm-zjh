package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmFollowUp;

public interface ICrmFollowUpService extends IService<CrmFollowUp> {

    IPage<CrmFollowUp> selectPageWithCondition(Page<CrmFollowUp> page, Long customerId, Long opportunityId,
                                                String type, Long creatorId, String startDate, String endDate);
}
