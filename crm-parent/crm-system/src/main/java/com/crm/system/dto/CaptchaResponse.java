package com.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码响应")
public class CaptchaResponse {

    @Schema(description = "验证码ID")
    private String captchaId;

    @Schema(description = "验证码图片Base64")
    private String captchaImage;
}
