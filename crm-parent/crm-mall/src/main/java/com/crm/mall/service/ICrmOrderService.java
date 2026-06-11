package com.crm.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ICrmOrderService extends IService<CrmOrder> {

    IPage<CrmOrder> selectPageWithCondition(Page<CrmOrder> page, Long customerId, String status,
                                             String startDate, String endDate, String keywords);

    CrmOrder selectWithItems(Long id);

    boolean updateStatus(Long id, Long customerId, String status);

    CrmOrder createOrder(Long customerId, List<Map<String, Object>> skuItems,
                         Long addressId, Long couponId, String remark);

    boolean payOrder(Long orderId, Long customerId, String paymentMethod);
}
