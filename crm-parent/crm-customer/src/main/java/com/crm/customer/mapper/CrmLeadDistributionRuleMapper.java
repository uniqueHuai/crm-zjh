package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.customer.entity.CrmLeadDistributionRule;
import com.crm.customer.entity.CrmLeadDistributionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CrmLeadDistributionRuleMapper extends BaseMapper<CrmLeadDistributionRule> {

    List<CrmLeadDistributionLog> selectLogsByRuleId(@Param("ruleId") Long ruleId,
                                                     @Param("offset") long offset,
                                                     @Param("limit") long limit);
}
