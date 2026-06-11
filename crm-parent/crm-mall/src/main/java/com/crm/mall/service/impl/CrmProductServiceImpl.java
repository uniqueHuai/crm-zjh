package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.mall.entity.CrmProduct;
import com.crm.mall.entity.CrmProductSku;
import com.crm.mall.mapper.CrmProductMapper;
import com.crm.mall.mapper.CrmProductSkuMapper;
import com.crm.mall.service.ICrmProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmProductServiceImpl extends ServiceImpl<CrmProductMapper, CrmProduct> implements ICrmProductService {

    private final CrmProductMapper productMapper;
    private final CrmProductSkuMapper skuMapper;

    @Override
    public IPage<CrmProduct> selectPageWithCondition(Page<CrmProduct> page, String keywords, Long categoryId, Integer status) {
        return productMapper.selectPage(page, new LambdaQueryWrapper<CrmProduct>()
                .eq(categoryId != null, CrmProduct::getCategoryId, categoryId)
                .eq(status != null, CrmProduct::getStatus, status)
                .like(keywords != null, CrmProduct::getName, keywords)
                .orderByAsc(CrmProduct::getSortOrder));
    }

    @Override
    public CrmProduct selectWithSkus(Long id) {
        CrmProduct product = getById(id);
        if (product != null) {
            product.setSkus(skuMapper.selectList(
                    new LambdaQueryWrapper<CrmProductSku>()
                            .eq(CrmProductSku::getProductId, id)
                            .orderByAsc(CrmProductSku::getId)));
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSku(CrmProductSku sku) {
        return skuMapper.insert(sku) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSku(CrmProductSku sku) {
        return skuMapper.updateById(sku) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSku(Long id) {
        return skuMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(Long id, Integer stock) {
        CrmProductSku sku = skuMapper.selectById(id);
        if (sku == null) return false;
        sku.setStock(stock);
        return skuMapper.updateById(sku) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().eq(CrmProduct::getId, id)
                .set(CrmProduct::getStatus, status)
                .update();
    }

    @Override
    public List<CrmProductSku> selectSkusByProductId(Long productId) {
        return skuMapper.selectList(
                new LambdaQueryWrapper<CrmProductSku>()
                        .eq(CrmProductSku::getProductId, productId)
                        .orderByAsc(CrmProductSku::getSortOrder));
    }
}
