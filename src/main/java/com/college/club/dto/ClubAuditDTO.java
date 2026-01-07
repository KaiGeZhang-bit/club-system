package com.college.club.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubAuditDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;

    @NotNull(message = "审核操作不能为空(1 通过， 2 驳回）")
    private Integer auditAction;

    private String auditRemark;

}
