package com.college.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuitClubDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "社团ID不能为空")
    private Long clubId;

    @NotBlank(message = "退出申请不能内容为空")
    private String applyReason;

}
