package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmSalesProduct;

public interface ICrmSalesProductService extends IService<CrmSalesProduct> {

    IPage<CrmSalesProduct> selectPageWithCondition(Page<CrmSalesProduct> page, Long categoryId, String keywords, Integer status);
}
