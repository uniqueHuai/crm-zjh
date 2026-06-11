package com.crm.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.sales.entity.CrmSalesProduct;
import org.apache.ibatis.annotations.Param;

public interface CrmSalesProductMapper extends BaseMapper<CrmSalesProduct> {

    IPage<CrmSalesProduct> selectPageWithCondition(Page<CrmSalesProduct> page,
                                                   @Param("keywords") String keywords,
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("status") Integer status);
}
