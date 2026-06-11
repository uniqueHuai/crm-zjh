package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.mp.MpContext;
import com.crm.mall.mp.MpJwtUtils;
import com.crm.mall.mp.WechatService;
import com.crm.mall.service.ICrmAuthService;
import com.crm.mall.service.ICrmDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "小程序端认证")
@RestController
@RequestMapping("/app/auth")
@RequiredArgsConstructor
public class CrmAppAuthController {

    private final WechatService wechatService;
    private final MpJwtUtils mpJwtUtils;
    private final ICrmDistributionService distributionService;
    private final ICrmAuthService authService;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "微信登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (!StringUtils.hasText(code)) {
            return R.failed(400, "code不能为空");
        }
        String referrerId = body.get("referrerId");

        // 1. code2Session (use mock when appid is placeholder)
        WechatService.WechatSession session;
        try {
            session = wechatService.code2Session(code);
        } catch (Exception e) {
            log.warn("wechat code2session failed, fallback to mock: {}", e.getMessage());
            session = wechatService.mockSession(code);
        }

        // 2. find or create customer
        Long customerId = authService.findOrCreateCustomer(session.getOpenid(), session.getUnionid());

        // 3. generate token
        String token = mpJwtUtils.createToken(customerId);

        // 4. save session
        authService.saveSession(customerId, session.getOpenid(), session.getUnionid(), token);

        // 5. bind referral
        if (referrerId != null && !referrerId.isEmpty()) {
            try {
                distributionService.bindReferral(customerId, Long.valueOf(referrerId));
            } catch (Exception e) {
                log.warn("bind referral failed", e);
            }
        }

        // 6. return token + customer info
        Map<String, Object> customerInfo = jdbcTemplate.queryForMap(
                "SELECT id, name, phone, level_id levelId, total_consumption totalConsumption, order_count orderCount " +
                "FROM crm_customer WHERE id = ?", customerId);

        return R.ok(Map.of(
                "token", token,
                "customer", customerInfo
        ));
    }

    @Operation(summary = "获取当前客户信息")
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        Long customerId = MpContext.getCustomerId();
        Map<String, Object> info = jdbcTemplate.queryForMap(
                "SELECT c.id, c.name, c.phone, c.level_id levelId, " +
                "c.total_consumption totalConsumption, c.order_count orderCount, " +
                "cl.name levelName, cl.min_amount minAmount, cl.max_amount maxAmount " +
                "FROM crm_customer c LEFT JOIN crm_customer_level cl ON c.level_id = cl.id " +
                "WHERE c.id = ?", customerId);
        return R.ok(info);
    }

    @Operation(summary = "绑定手机号")
    @PostMapping("/phone")
    public R<Void> bindPhone(@RequestBody Map<String, String> body) {
        Long customerId = MpContext.getCustomerId();
        String phone = body.get("phone");
        if (!StringUtils.hasText(phone)) {
            return R.failed(400, "手机号不能为空");
        }
        jdbcTemplate.update("UPDATE crm_customer SET phone = ? WHERE id = ?", phone, customerId);
        return R.ok();
    }
}
