package com.college.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入社团入参DTO（无枚举，纯基础字段）
 */
@Data
public class JoinClubDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "社团ID不能为空")
    private Long clubId;
}