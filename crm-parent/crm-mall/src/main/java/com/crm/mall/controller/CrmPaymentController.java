package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.service.ICrmPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "支付管理", description = "支付创建、回调、退款")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CrmPaymentController {

    private final ICrmPaymentService paymentService;

    @Operation(summary = "创建支付")
    @PostMapping("/app/payments/create-payment")
    public R<Map<String, Object>> createPayment(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String paymentMethod = (String) body.get("paymentMethod");
        String openId = (String) body.get("openId");
        return R.ok(paymentService.createPayment(orderId, paymentMethod, openId));
    }

    @Operation(summary = "支付回调")
    @PostMapping("/payments/notify")
    public R<String> notify(@RequestBody Map<String, Object> params) {
        paymentService.handleNotify(params);
        return R.ok("success");
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/app/payments/{paymentId}/status")
    public R<String> queryStatus(@PathVariable Long paymentId) {
        return R.ok(paymentService.queryStatus(paymentId));
    }

    @Operation(summary = "退款")
    @PostMapping("/payments/refund")
    public R<Void> refund(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(body.get("refundAmount").toString()));
        String reason = (String) body.get("reason");
        paymentService.refund(orderId, amount, reason);
        return R.ok();
    }
}
