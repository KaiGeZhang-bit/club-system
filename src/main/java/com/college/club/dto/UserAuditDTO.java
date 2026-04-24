package com.college.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAuditDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    private String auditRemark;
}
