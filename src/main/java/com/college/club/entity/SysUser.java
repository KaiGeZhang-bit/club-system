package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user") // 绑定你的sys_user表
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;   // 你的表：用户名（学号/工号）
    private String password;   // 你的表：密码（BCrypt加密）
    private String name;       // 你的表：真实姓名（必填）
    private Integer role;      // 你的表：角色（0普通成员/1社团负责人/2管理员）
    private String phone;      // 你的表：手机号（可选）
    private String avatar;     // 你的表：头像URL（可选）
    private Integer status;    // 你的表：状态（0禁用/1正常）

    private LocalDateTime createTime; // 你的表：创建时间
    private LocalDateTime updateTime; // 你的表：更新时间
}