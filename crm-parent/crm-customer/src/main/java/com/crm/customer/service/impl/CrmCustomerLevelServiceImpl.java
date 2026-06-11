package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.customer.entity.CrmCustomerLevel;
import com.crm.customer.entity.CrmCustomerLevelRule;
import com.crm.customer.mapper.CrmCustomerLevelMapper;
import com.crm.customer.mapper.CrmCustomerLevelRuleMapper;
import com.crm.customer.service.ICrmCustomerLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmCustomerLevelServiceImpl extends ServiceImpl<CrmCustomerLevelMapper, CrmCustomerLevel>
        implements ICrmCustomerLevelService {

    private final CrmCustomerLevelMapper levelMapper;
    private final CrmCustomerLevelRuleMapper levelRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setLevelRules(Long levelId, Map<String, Object> rules) {
        levelRuleMapper.delete(new LambdaQueryWrapper<CrmCustomerLevelRule>()
                .eq(CrmCustomerLevelRule::getLevelId, levelId));

        Map<String, Object> upgrade = (Map<String, Object>) rules.get("upgradeRule");
        Map<String, Object> downgrade = (Map<String, Object>) rules.get("downgradeRule");
        String cycle = (String) rules.get("evaluateCycle");

        if (upgrade != null) {
            CrmCustomerLevelRule rule = new CrmCustomerLevelRule();
            rule.setLevelId(levelId);
            rule.setRuleType("upgrade");
            rule.setConditionField((String) upgrade.get("condition"));
            rule.setConditionOperator("gte");
            rule.setConditionValue(new BigDecimal(upgrade.get("minAmount").toString()));
            rule.setPeriodDays(upgrade.get("periodDays") != null ? Integer.parseInt(upgrade.get("periodDays").toString()) : null);
            rule.setEvaluateCycle(cycle != null ? cycle : "monthly");
            rule.setStatus(1);
            levelRuleMapper.insert(rule);
        }
        if (downgrade != null) {
            CrmCustomerLevelRule rule = new CrmCustomerLevelRule();
            rule.setLevelId(levelId);
            rule.setRuleType("downgrade");
            rule.setConditionField((String) downgrade.get("condition"));
            rule.setConditionOperator("gte");
            rule.setConditionValue(new BigDecimal(downgrade.get("maxDays").toString()));
            rule.setEvaluateCycle(cycle != null ? cycle : "monthly");
            rule.setStatus(1);
            levelRuleMapper.insert(rule);
        }
        return true;
    }

    @Override
    public List<CrmCustomerLevelRule> getLevelRules(Long levelId) {
        return levelRuleMapper.selectList(
                new LambdaQueryWrapper<CrmCustomerLevelRule>().eq(CrmCustomerLevelRule::getLevelId, levelId));
    }

    @Override
    public boolean evaluateAll() {
        // TODO: implement level evaluation logic
        return true;
    }
}
