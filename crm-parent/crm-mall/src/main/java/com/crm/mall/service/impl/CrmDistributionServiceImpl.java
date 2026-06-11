package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.mall.entity.CrmCommission;
import com.crm.mall.entity.CrmDistributionRule;
import com.crm.mall.entity.CrmDistributor;
import com.crm.mall.mapper.CrmCommissionMapper;
import com.crm.mall.mapper.CrmDistributionRuleMapper;
import com.crm.mall.mapper.CrmDistributorMapper;
import com.crm.mall.service.ICrmDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmDistributionServiceImpl extends ServiceImpl<CrmDistributionRuleMapper, CrmDistributionRule>
        implements ICrmDistributionService {

    private final CrmDistributorMapper distributorMapper;
    private final CrmCommissionMapper commissionMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public CrmDistributor getDistributorByUserId(Long userId) {
        return distributorMapper.selectOne(
                new LambdaQueryWrapper<CrmDistributor>()
                        .eq(CrmDistributor::getUserId, userId));
    }

    @Override
    public List<CrmDistributor> selectTeam(Long distributorId) {
        return distributorMapper.selectList(
                new LambdaQueryWrapper<CrmDistributor>()
                        .eq(CrmDistributor::getParentId, distributorId));
    }

    @Override
    public List<CrmCommission> selectCommissions(Long distributorId) {
        return commissionMapper.selectList(
                new LambdaQueryWrapper<CrmCommission>()
                        .eq(CrmCommission::getDistributorId, distributorId)
                        .orderByDesc(CrmCommission::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindReferral(Long customerId, Long referrerId) {
        if (customerId.equals(referrerId)) return;
        CrmDistributor existing = getDistributorByUserId(customerId);
        if (existing != null) return;

        // ensure referrer is a distributor
        CrmDistributor parent = getDistributorByUserId(referrerId);
        if (parent == null) {
            parent = new CrmDistributor();
            parent.setUserId(referrerId);
            parent.setLevel("1");
            parent.setTotalCommission(BigDecimal.ZERO);
            parent.setWithdrawable(BigDecimal.ZERO);
            parent.setCreatedAt(LocalDateTime.now());
            distributorMapper.insert(parent);
        }

        CrmDistributor distributor = new CrmDistributor();
        distributor.setUserId(customerId);
        distributor.setParentId(parent.getId());
        distributor.setLevel("1");
        distributor.setTotalCommission(BigDecimal.ZERO);
        distributor.setWithdrawable(BigDecimal.ZERO);
        distributor.setCreatedAt(LocalDateTime.now());
        distributorMapper.insert(distributor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateCommission(Long orderId, Long customerId, BigDecimal amount) {
        CrmDistributor distributor = getDistributorByUserId(customerId);
        if (distributor == null || distributor.getParentId() == null) return;

        // get first active rule
        CrmDistributionRule rule = lambdaQuery()
                .eq(CrmDistributionRule::getStatus, 1)
                .last("LIMIT 1")
                .one();
        if (rule == null) return;

        BigDecimal rate = rule.getCommissionRate() != null ? rule.getCommissionRate() : BigDecimal.valueOf(0.05);
        BigDecimal commission = amount.multiply(rate);

        CrmCommission c = new CrmCommission();
        c.setDistributorId(distributor.getParentId());
        c.setOrderId(orderId);
        c.setAmount(commission);
        c.setRate(rate);
        c.setStatus("pending");
        c.setCreatedAt(LocalDateTime.now());
        commissionMapper.insert(c);

        distributorMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CrmDistributor>()
                        .eq(CrmDistributor::getId, distributor.getParentId())
                        .setSql("total_commission = COALESCE(total_commission, 0) + " + commission));
    }

    @Override
    public Map<String, Object> getDistributorProfile(Long customerId) {
        Map<String, Object> result = new HashMap<>();
        CrmDistributor distributor = getDistributorByUserId(customerId);
        if (distributor == null) {
            result.put("isDistributor", false);
            return result;
        }
        result.put("isDistributor", true);
        result.put("distributor", distributor);
        result.put("teamCount", distributorMapper.selectCount(
                new LambdaQueryWrapper<CrmDistributor>()
                        .eq(CrmDistributor::getParentId, distributor.getId())));
        result.put("commissions", selectCommissions(distributor.getId()));
        return result;
    }
}
