package com.college.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "经费审批表单")
public class ApprovalDTO {

    @NotNull(message = "审批动作不能为空")
    @Schema(description = "审批动作：1通过，2驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer action;

    @Size(max = 500, message = "审批意见不能超过500字")
    @Schema(description = "审批意见")
    private String remark;
}