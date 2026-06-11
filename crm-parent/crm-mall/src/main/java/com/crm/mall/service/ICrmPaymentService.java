package com.crm.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmPayment;

import java.math.BigDecimal;
import java.util.Map;

public interface ICrmPaymentService extends IService<CrmPayment> {

    Map<String, Object> createPayment(Long orderId, String paymentMethod, String openId);

    boolean handleNotify(Map<String, Object> params);

    String queryStatus(Long paymentId);

    boolean refund(Long orderId, BigDecimal amount, String reason);
}
