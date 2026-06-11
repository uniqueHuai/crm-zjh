package com.crm.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.sales.entity.CrmQuotation;
import org.apache.ibatis.annotations.Param;

public interface CrmQuotationMapper extends BaseMapper<CrmQuotation> {

    IPage<CrmQuotation> selectPageWithCondition(Page<CrmQuotation> page,
                                                @Param("keywords") String keywords,
                                                @Param("customerId") Long customerId,
                                                @Param("opportunityId") Long opportunityId,
                                                @Param("status") String status,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);
}
