package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.customer.entity.CrmLead;
import org.apache.ibatis.annotations.Param;

public interface CrmLeadMapper extends BaseMapper<CrmLead> {

    IPage<CrmLead> selectPageWithCondition(Page<CrmLead> page, @Param("keywords") String keywords,
                                            @Param("status") String status, @Param("sourceChannel") String sourceChannel,
                                            @Param("ownerId") Long ownerId, @Param("assignType") String assignType,
                                            @Param("province") String province, @Param("city") String city,
                                            @Param("industry") String industry, @Param("startDate") String startDate,
                                            @Param("endDate") String endDate, @Param("poolReturn") Boolean poolReturn);
}
