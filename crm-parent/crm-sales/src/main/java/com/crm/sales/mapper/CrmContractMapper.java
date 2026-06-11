package com.crm.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.sales.entity.CrmContract;
import org.apache.ibatis.annotations.Param;

public interface CrmContractMapper extends BaseMapper<CrmContract> {

    IPage<CrmContract> selectPageWithCondition(Page<CrmContract> page,
                                               @Param("keywords") String keywords,
                                               @Param("customerId") Long customerId,
                                               @Param("status") String status,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);
}
