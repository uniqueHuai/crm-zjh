package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.sales.entity.CrmSalesProduct;
import com.crm.sales.mapper.CrmSalesProductMapper;
import com.crm.sales.service.ICrmSalesProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmSalesProductServiceImpl extends ServiceImpl<CrmSalesProductMapper, CrmSalesProduct>
        implements ICrmSalesProductService {

    private final CrmSalesProductMapper productMapper;

    @Override
    public IPage<CrmSalesProduct> selectPageWithCondition(Page<CrmSalesProduct> page, Long categoryId,
                                                           String keywords, Integer status) {
        return productMapper.selectPageWithCondition(page, keywords, categoryId, status);
    }
}
