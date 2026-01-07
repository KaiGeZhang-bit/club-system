package com.college.club.common.vo;

import lombok.Data;

/**
 * 注册成功返回结果
 */
@Data
public class RegisterVO {
    private Long userId; // 注册成功的用户ID
    private String username; // 登录账号
    private String name; // 真实姓名
    private Integer role; // 角色（1=学生，2=老师）
    private String message; // 提示信息（如“注册成功，请登录”）
}