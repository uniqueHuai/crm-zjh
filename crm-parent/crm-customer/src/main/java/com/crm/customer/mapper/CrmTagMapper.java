package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.customer.entity.CrmTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CrmTagMapper extends BaseMapper<CrmTag> {

    List<CrmTag> selectByCustomerId(@Param("customerId") Long customerId);
}
