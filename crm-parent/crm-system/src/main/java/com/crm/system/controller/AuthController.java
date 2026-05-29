package com.crm.system.controller;

import com.crm.common.model.R;
import com.crm.framework.security.LoginUser;
import com.crm.framework.security.SecurityUtils;
import com.crm.system.dto.LoginRequest;
import com.crm.system.dto.LoginResponse;
import com.crm.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录、登出、刷新Token")
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
                .roles(loginUser.getRoles().stream().toList())
                .permissions(loginUser.getPermissions())
                .build();
        return R.ok(userInfo);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> updatePassword(@RequestBody LoginRequest request) {
        Long userId = SecurityUtils.getUserId();
        userService.resetPassword(userId, request.getPassword());
        return R.ok();
    }
}
