package com.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问Token")
    private String accessToken;

    @Schema(description = "刷新Token")
    private String refreshToken;

    @Schema(description = "过期时间（秒）")
    private Long expiresIn;

    @Schema(description = "Token类型")
    private String tokenType;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "登录用户信息")
    public static class UserInfo {
        private Long userId;
        private String username;
        private String realName;
        private String avatar;
        private Long deptId;
        private String deptName;
        private List<String> roles;
        private Set<String> permissions;
    }
}
