package com.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户导入结果")
public class UserImportResult {

    @Schema(description = "总条数")
    private int totalCount;

    @Schema(description = "成功数")
    private int successCount;

    @Schema(description = "失败数")
    private int failCount;

    @Schema(description = "失败明细")
    private List<FailDetail> failDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailDetail {
        private int row;
        private String reason;
        private String phone;
    }
}
