package com.college.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录参数接收类（DTO）
 * 仅接收登录必需的「用户名+密码」，适配你的sys_user表登录逻辑
 */
@Data // 必须加！自动生成get/set，否则前端参数无法传递到后端
public class SysUserLoginDTO {
    /** 登录用户名（对应你的sys_user表username字段） */
    @NotBlank(message = "用户名不能为空") // 强制校验：前端不传则返回该提示
    private String username;

    /** 登录密码（明文，后端会加密后和数据库对比） */
    @NotBlank(message = "密码不能为空")
    private String password;


}