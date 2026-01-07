package com.college.club.common.vo;

import lombok.Data;

/**
 * 登录成功返回结果VO
 */
@Data
public class LoginVO {
    private Long userId; // 用户ID（sys_user表的id）
    private String username; // 登录账号
    private String name; // 真实姓名
    private Integer role; // 角色：1=学生，2=老师
    private String sessionId; // SessionID（登录态标识）
    private String message; // 提示信息（如“登录成功”）
}