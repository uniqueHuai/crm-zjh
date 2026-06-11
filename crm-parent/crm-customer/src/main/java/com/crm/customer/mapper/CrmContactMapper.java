package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.customer.entity.CrmContact;
import org.apache.ibatis.annotations.Param;

public interface CrmContactMapper extends BaseMapper<CrmContact> {

    IPage<CrmContact> selectPageWithCondition(Page<CrmContact> page,
                                              @Param("keywords") String keywords,
                                              @Param("customerId") Long customerId,
                                              @Param("isDecisionMaker") Boolean isDecisionMaker);
}
