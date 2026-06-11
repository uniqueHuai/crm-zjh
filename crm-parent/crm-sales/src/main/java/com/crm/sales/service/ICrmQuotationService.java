package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmQuotation;

import java.util.Map;

public interface ICrmQuotationService extends IService<CrmQuotation> {

    IPage<CrmQuotation> selectPageWithCondition(Page<CrmQuotation> page, String keywords, Long customerId, Long opportunityId,
                                                 String status, String startDate, String endDate);

    CrmQuotation selectWithItems(Long id);

    boolean submitApproval(Long id, String remark);

    boolean approve(Long id, String comment);

    boolean reject(Long id, String comment);

    boolean voidQuotation(Long id, String reason);

    Long generateContract(Long id);
}
