package com.crm.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmCustomerLevel;
import com.crm.customer.entity.CrmCustomerLevelRule;

import java.util.List;
import java.util.Map;

public interface ICrmCustomerLevelService extends IService<CrmCustomerLevel> {

    boolean setLevelRules(Long levelId, Map<String, Object> rules);

    List<CrmCustomerLevelRule> getLevelRules(Long levelId);

    boolean evaluateAll();
}
