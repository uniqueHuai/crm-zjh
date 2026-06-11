package com.crm.mall.mp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    @Value("${mp.wechat.appid:}")
    private String appId;

    @Value("${mp.wechat.secret:}")
    private String secret;

    private final ObjectMapper objectMapper;

    private static final String JSCODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    /**
     * code2Session: exchange code for openid/session_key
     */
    public WechatSession code2Session(String code) {
        // Dev mode: skip real WeChat API when using placeholder appid
        if ("wx".equals(appId) || appId == null || appId.isBlank()) {
            log.warn("placeholder appid detected, returning mock session directly");
            return mockSession(code);
        }
        try {
            var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(5000);
            RestTemplate rt = new RestTemplate(factory);
            String resp = rt.getForObject(JSCODE2SESSION_URL, String.class,
                    Map.of("appid", appId, "secret", secret, "code", code));
            JsonNode json = objectMapper.readTree(resp);
            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                log.error("wechat code2session error: {} - {}", json.get("errcode"), json.get("errmsg"));
                throw new RuntimeException("微信登录失败: " + json.get("errmsg").asText());
            }
            WechatSession session = new WechatSession();
            session.setOpenid(json.get("openid").asText());
            if (json.has("unionid")) session.setUnionid(json.get("unionid").asText());
            if (json.has("session_key")) session.setSessionKey(json.get("session_key").asText());
            return session;
        } catch (Exception e) {
            log.error("wechat code2session call failed", e);
            throw new RuntimeException("微信登录异常", e);
        }
    }

    @Data
    public static class WechatSession {
        private String openid;
        private String unionid;
        private String sessionKey;
    }

    /**
     * for dev/test: mock session when no wechat config
     */
    public WechatSession mockSession(String code) {
        log.warn("using mock wechat session for code: {}", code);
        WechatSession session = new WechatSession();
        session.setOpenid("mock_openid_" + code);
        session.setUnionid("mock_unionid_" + code);
        session.setSessionKey("mock_session_key");
        return session;
    }
}
