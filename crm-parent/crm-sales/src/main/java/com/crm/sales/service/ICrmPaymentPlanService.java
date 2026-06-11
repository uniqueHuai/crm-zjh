package com.crm.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmPaymentPlan;

import java.util.List;
import java.util.Map;

public interface ICrmPaymentPlanService extends IService<CrmPaymentPlan> {

    boolean settle(Long id, Map<String, Object> params);

    List<CrmPaymentPlan> batchGenerate(Long contractId, Map<String, Object> params);
}
