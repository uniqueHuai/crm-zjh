package com.crm.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmProduct;
import com.crm.mall.entity.CrmProductSku;

import java.util.List;

public interface ICrmProductService extends IService<CrmProduct> {

    IPage<CrmProduct> selectPageWithCondition(Page<CrmProduct> page, String keywords, Long categoryId, Integer status);

    CrmProduct selectWithSkus(Long id);

    boolean createSku(CrmProductSku sku);

    boolean updateSku(CrmProductSku sku);

    boolean deleteSku(Long id);

    boolean updateStock(Long id, Integer stock);

    boolean updateStatus(Long id, Integer status);

    List<CrmProductSku> selectSkusByProductId(Long productId);
}
