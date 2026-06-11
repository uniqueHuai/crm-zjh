package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmContract;

import java.util.Map;

public interface ICrmContractService extends IService<CrmContract> {

    IPage<CrmContract> selectPageWithCondition(Page<CrmContract> page, String keywords, Long customerId, String status,
                                                String startDate, String endDate);

    CrmContract selectWithItems(Long id);

    boolean sign(Long id, Map<String, Object> params);

    boolean cancel(Long id, Map<String, Object> params);

    Long renewal(Long id, Map<String, Object> params);
}
