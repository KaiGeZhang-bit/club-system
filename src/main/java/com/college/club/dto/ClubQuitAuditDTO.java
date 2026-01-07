package com.college.club.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubQuitAuditDTO {
    @NotNull(message = "社团ID不能为空")
    private Long id;

    @NotNull(message = "要退出的社团Id不能为空")
    private Long clubId;

    @NotNull(message = "状态只能为1.2，3，且不能不填")
    private Integer status;
}
