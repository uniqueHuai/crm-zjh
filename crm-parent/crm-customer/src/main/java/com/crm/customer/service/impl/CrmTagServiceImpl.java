package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.customer.entity.CrmAutoTagRule;
import com.crm.customer.entity.CrmTag;
import com.crm.customer.mapper.CrmAutoTagRuleMapper;
import com.crm.customer.mapper.CrmTagMapper;
import com.crm.customer.service.ICrmTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmTagServiceImpl extends ServiceImpl<CrmTagMapper, CrmTag> implements ICrmTagService {

    private final CrmTagMapper tagMapper;
    private final CrmAutoTagRuleMapper autoTagRuleMapper;

    @Override
    public List<CrmTag> selectByCustomerId(Long customerId) {
        return tagMapper.selectByCustomerId(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createAutoTagRule(CrmAutoTagRule rule) {
        return autoTagRuleMapper.insert(rule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAutoTagRule(CrmAutoTagRule rule) {
        return autoTagRuleMapper.updateById(rule) > 0;
    }

    @Override
    public IPage<CrmAutoTagRule> selectAutoRulePage(Page<CrmAutoTagRule> page) {
        return autoTagRuleMapper.selectPage(page, null);
    }

    @Override
    public boolean executeAutoRule(Long ruleId) {
        // TODO: implement auto rule execution logic
        return true;
    }
}
