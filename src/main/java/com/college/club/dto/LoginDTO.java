package com.college.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录入参DTO（和你项目的命名风格保持一致）
 */
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username; // 登录账号

    @NotBlank(message = "密码不能为空")
    private String password; // 登录密码（明文）
}