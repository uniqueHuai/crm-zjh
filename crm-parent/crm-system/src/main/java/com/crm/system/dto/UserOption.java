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
@Schema(description = "用户选项（下拉框用）")
public class UserOption {

    @Schema(description = "用户ID")
    private Long value;

    @Schema(description = "用户姓名")
    private String label;

    @Schema(description = "部门名称")
    private String deptName;
}
