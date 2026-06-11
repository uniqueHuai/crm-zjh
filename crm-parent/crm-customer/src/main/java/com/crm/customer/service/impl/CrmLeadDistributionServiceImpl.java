package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.customer.entity.CrmLeadDistributionLog;
import com.crm.customer.entity.CrmLeadDistributionRule;
import com.crm.customer.mapper.CrmLeadDistributionRuleMapper;
import com.crm.customer.service.ICrmLeadDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmLeadDistributionServiceImpl extends ServiceImpl<CrmLeadDistributionRuleMapper, CrmLeadDistributionRule>
        implements ICrmLeadDistributionService {

    private final CrmLeadDistributionRuleMapper ruleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeRule(Long ruleId) {
        // TODO: implement distribution logic based on strategy
        return true;
    }

    @Override
    public Map<String, Object> selectLogsByRuleId(Long ruleId, long page, long size) {
        long offset = (page - 1) * size;
        List<CrmLeadDistributionLog> logs = ruleMapper.selectLogsByRuleId(ruleId, offset, size);
        long total = 0; // TODO: count total
        Map<String, Object> result = new HashMap<>();
        result.put("records", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }
}
