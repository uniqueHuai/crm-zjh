package com.crm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmAutoTagRule;
import com.crm.customer.entity.CrmTag;

import java.util.List;

public interface ICrmTagService extends IService<CrmTag> {

    List<CrmTag> selectByCustomerId(Long customerId);

    boolean createAutoTagRule(CrmAutoTagRule rule);

    boolean updateAutoTagRule(CrmAutoTagRule rule);

    IPage<CrmAutoTagRule> selectAutoRulePage(Page<CrmAutoTagRule> page);

    boolean executeAutoRule(Long ruleId);
}
