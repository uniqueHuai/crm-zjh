package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.mall.entity.CrmPayment;
import com.crm.mall.mapper.CrmPaymentMapper;
import com.crm.mall.service.ICrmPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmPaymentServiceImpl extends ServiceImpl<CrmPaymentMapper, CrmPayment> implements ICrmPaymentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Long orderId, String paymentMethod, String openId) {
        CrmPayment payment = new CrmPayment();
        payment.setOrderId(orderId);
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("pending");
        save(payment);
        // TODO: call third-party payment SDK to get pay params
        return Map.of(
                "paymentId", payment.getId(),
                "payParams", Map.of(
                        "appId", "wx...",
                        "timeStamp", String.valueOf(System.currentTimeMillis() / 1000),
                        "nonceStr", UUID.randomUUID().toString().replace("-", ""),
                        "package", "prepay_id=wx...",
                        "signType", "RSA",
                        "paySign", "signature"
                )
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(Map<String, Object> params) {
        Long paymentId = Long.valueOf(params.get("paymentId").toString());
        CrmPayment payment = getById(paymentId);
        if (payment == null) return false;
        payment.setStatus("success");
        payment.setTransactionId((String) params.get("transactionId"));
        payment.setPaidAt(LocalDateTime.now());
        updateById(payment);
        return true;
    }

    @Override
    public String queryStatus(Long paymentId) {
        CrmPayment payment = getById(paymentId);
        return payment != null ? payment.getStatus() : "not_found";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refund(Long orderId, BigDecimal amount, String reason) {
        // TODO: call third-party refund API
        return true;
    }
}
