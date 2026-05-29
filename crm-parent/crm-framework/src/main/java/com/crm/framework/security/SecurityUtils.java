package com.crm.framework.security;

import com.crm.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser)) {
            throw new BizException(401, "未登录或 Token 已过期");
        }
        return (LoginUser) auth.getPrincipal();
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static Long getDeptId() {
        return getLoginUser().getDeptId();
    }

    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    public static Set<String> getPermissions() {
        return getLoginUser().getPermissions();
    }

    public static boolean isAdmin() {
        return getLoginUser().getRoles().contains("admin");
    }

    /** 加密密码 */
    public static String encryptPassword(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 验证密码 */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /** 获取当前认证信息 */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
