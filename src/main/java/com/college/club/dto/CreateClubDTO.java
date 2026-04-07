package com.college.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建社团入参DTO（匹配ClubInfo实体字段）
 */
@Data
public class CreateClubDTO {
    @NotBlank(message = "社团名称不能为空")
    private String clubName; // 社团名称

    @NotBlank(message = "社团类型不能为空")
    private String clubType; // 社团类型：学术科技类、文化体育类、公益服务类

    private String intro; // 社团简介（可选）

//    @NotNull(message = "负责人ID不能为空")
//    private Long leaderId; // 负责人ID（关联sys_user.id）

    @NotBlank(message = "负责人姓名不能为空")
    private String leaderName; // 负责人姓名

    @NotNull(message = "指导老师ID不能为空")
    private Long teacherId; // 指导老师ID（关联sys_user.id）

    @NotBlank(message = "指导老师姓名不能为空")
    private String teacherName; // 指导老师姓名
}