package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmInvoice;

import java.util.Map;

public interface ICrmInvoiceService extends IService<CrmInvoice> {

    IPage<CrmInvoice> selectPageWithCondition(Page<CrmInvoice> page, Long customerId, Long contractId,
                                               String status, String startDate, String endDate);

    boolean issue(Long id, Map<String, Object> params);

    boolean ship(Long id, Map<String, Object> params);

    boolean confirm(Long id);

    boolean cancelInvoice(Long id, Map<String, Object> params);
}
