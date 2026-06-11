package com.crm.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.sales.entity.CrmOpportunity;
import org.apache.ibatis.annotations.Param;

public interface CrmOpportunityMapper extends BaseMapper<CrmOpportunity> {

    IPage<CrmOpportunity> selectPageWithCondition(Page<CrmOpportunity> page,
                                                  @Param("keywords") String keywords,
                                                  @Param("stageId") Long stageId,
                                                  @Param("customerId") Long customerId,
                                                  @Param("ownerId") Long ownerId);
}
