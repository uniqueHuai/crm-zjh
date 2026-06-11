package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.customer.entity.CrmCustomer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CrmCustomerMapper extends BaseMapper<CrmCustomer> {

    IPage<CrmCustomer> selectPageWithCondition(Page<CrmCustomer> page, @Param("keywords") String keywords,
                                                @Param("levelId") Long levelId, @Param("ownerId") Long ownerId,
                                                @Param("sourceChannel") String sourceChannel,
                                                @Param("province") String province, @Param("city") String city,
                                                @Param("isSleeping") Boolean isSleeping,
                                                @Param("startDate") String startDate, @Param("endDate") String endDate);

    IPage<CrmCustomer> selectPageBySegment(Page<CrmCustomer> page, @Param("segmentId") Long segmentId);

    List<String> selectPhoneByCustomerIds(@Param("ids") List<Long> ids);
}
