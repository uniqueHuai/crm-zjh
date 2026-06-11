package com.crm.system.controller;

import com.crm.common.model.R;
import com.crm.framework.security.LoginUser;
import com.crm.framework.security.SecurityUtils;
import com.crm.system.dto.*;
import com.crm.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "认证管理", description = "登录、登出、刷新Token、修改密码、验证码、公钥")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ISysUserService userService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(userService.login(request));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // TODO: implement refresh token logic
        return R.failed(501, "刷新Token功能待实现");
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        // JWT 无状态，前端清除 Token 即可
        // 可选：将 Token 加入黑名单
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<LoginResponse.UserInfo> getUserInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .avatar(loginUser.getAvatar())
                .phone(loginUser.getPhone())
                .email(loginUser.getEmail())
                .deptId(loginUser.getDeptId())
                .deptName(loginUser.getDeptName())
                .lastLoginAt(loginUser.getLastLoginAt())
                .roles(loginUser.getRoles().stream().toList())
                .permissions(loginUser.getPermissions())
                .build();
        return R.ok(userInfo);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return R.failed(400001, "两次输入密码不一致");
        }
        Long userId = SecurityUtils.getUserId();
        userService.resetPassword(userId, request.getNewPassword());
        return R.ok();
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<CaptchaResponse> captcha() {
        // TODO: integrate with captcha service (easy-captcha / google captcha)
        CaptchaResponse response = CaptchaResponse.builder()
                .captchaId(UUID.randomUUID().toString())
                .captchaImage("data:image/png;base64,")
                .build();
        return R.ok(response);
    }

    @Operation(summary = "获取公钥（密码 RSA 加密）")
    @GetMapping("/public-key")
    public R<PublicKeyResponse> publicKey() {
        // TODO: integrate with RSA key service to return actual public key
        return R.ok(PublicKeyResponse.builder()
                .publicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC...")
                .build());
    }
}
