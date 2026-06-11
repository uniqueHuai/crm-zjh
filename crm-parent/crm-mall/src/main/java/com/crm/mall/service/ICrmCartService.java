package com.crm.mall.service;

import com.crm.mall.entity.CrmCartItem;

import java.util.List;

public interface ICrmCartService {

    List<CrmCartItem> listByCustomerId(Long customerId);

    boolean addItem(Long customerId, Long skuId, Integer quantity);

    boolean updateQuantity(Long id, Long customerId, Integer quantity);

    boolean toggleSelected(Long id, Long customerId, Boolean selected);

    boolean deleteItem(Long id, Long customerId);

    void clearByCustomerId(Long customerId, List<Long> skuIds);
}
