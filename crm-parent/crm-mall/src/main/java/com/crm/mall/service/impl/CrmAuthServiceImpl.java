package com.crm.mall.service.impl;

import com.crm.common.exception.BizException;
import com.crm.mall.service.ICrmAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmAuthServiceImpl implements ICrmAuthService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long findOrCreateCustomer(String openid, String unionid) {
        // 先通过 openid 查找已有客户
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id FROM crm_customer WHERE wechat_openid = ?", openid);
            return ((Number) row.get("id")).longValue();
        } catch (Exception ignored) {}

        // 再尝试通过 unionid 查找（可能是同一用户不同小程序）
        if (StringUtils.hasText(unionid)) {
            try {
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT id FROM crm_customer WHERE wechat_unionid = ?", unionid);
                // 查到后回填 openid
                jdbcTemplate.update(
                        "UPDATE crm_customer SET wechat_openid = ? WHERE id = ?", openid, row.get("id"));
                return ((Number) row.get("id")).longValue();
            } catch (Exception ignored) {}
        }

        // 原子化创建：INSERT ... ON CONFLICT ... RETURNING id
        // 无论 INSERT 还是命中已有记录，都直接返回 id，避免并发竞态
        String name = "微信用户" + openid.substring(Math.max(0, openid.length() - 8));
        return jdbcTemplate.queryForObject("""
            INSERT INTO crm_customer (name, wechat_openid, wechat_unionid, source_channel, status, created_at, updated_at)
            VALUES (?, ?, ?, 'mini_program', 1, NOW(), NOW())
            ON CONFLICT (wechat_openid) DO UPDATE SET
                wechat_unionid = COALESCE(EXCLUDED.wechat_unionid, crm_customer.wechat_unionid),
                updated_at = NOW()
            RETURNING id
            """, Long.class, name, openid, unionid);
    }

    @Override
    public void saveSession(Long customerId, String openid, String unionid, String token) {
        jdbcTemplate.update("""
            INSERT INTO mp_session (customer_id, openid, unionid, token, token_expire_at, last_login_at)
            VALUES (?, ?, ?, ?, NOW() + INTERVAL '30 days', NOW())
            ON CONFLICT (openid) DO UPDATE SET
                token = EXCLUDED.token,
                token_expire_at = EXCLUDED.token_expire_at,
                last_login_at = NOW()
            """, customerId, openid, unionid, token);
    }
}
