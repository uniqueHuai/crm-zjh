package com.crm.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmLeadDistributionLog;
import com.crm.customer.entity.CrmLeadDistributionRule;

import java.util.List;
import java.util.Map;

public interface ICrmLeadDistributionService extends IService<CrmLeadDistributionRule> {

    boolean executeRule(Long ruleId);

    Map<String, Object> selectLogsByRuleId(Long ruleId, long page, long size);
}
