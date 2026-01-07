package com.college.club.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 注册入参DTO
 */
@Data
public class SysUserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20位之间")
    private String username; // 登录账号（唯一）

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password; // 登录密码（明文，后端加密存储）

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 10, message = "真实姓名长度必须在2-10位之间")
    private String name; // 真实姓名（如“张三”“李老师”）

    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色只能是0（管理员）、1（学生）、2（老师）")
    @Max(value = 2, message = "角色只能是0（管理员）、1（学生）、2（老师）")
    private Integer role; // 角色：1=学生，2=老师（仅支持这两种，后端校验）
}