package com.crm.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmDistributionRule;
import com.crm.mall.entity.CrmDistributor;
import com.crm.mall.entity.CrmCommission;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ICrmDistributionService extends IService<CrmDistributionRule> {

    CrmDistributor getDistributorByUserId(Long userId);

    List<CrmDistributor> selectTeam(Long distributorId);

    List<CrmCommission> selectCommissions(Long distributorId);

    void bindReferral(Long customerId, Long referrerId);

    void calculateCommission(Long orderId, Long customerId, BigDecimal amount);

    Map<String, Object> getDistributorProfile(Long customerId);
}
