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
@Schema(description = "公钥响应")
public class PublicKeyResponse {

    @Schema(description = "RSA 公钥（Base64）")
    private String publicKey;
}
