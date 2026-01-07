package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_info") // 对应数据库表名
public class ClubInfo {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id; // 社团ID
    private String clubName; // 社团名称
    private String clubType; // 社团类型：学术科技类、文化体育类、公益服务类
    private String intro; // 社团简介
    private Long leaderId; // 负责人ID（关联sys_user.id）
    private String leaderName; // 负责人姓名
    private Long teacherId; // 指导老师ID（关联sys_user.id）
    private String teacherName; // 指导老师姓名
    private Integer status; // 状态：0待审核, 1正常, 2解散
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

    //新增创建社团审核相关字段
    @TableField("auditor_id") // 对应数据库的auditor_id
    private Long auditorId;  //审核人ID
    @TableField("audit_remark") // 对应数据库的audit_remark
    private String auditRemark; //审核备注，驳回时必填
    @TableField("audit_time") // 对应数据库的audit_time
    private LocalDateTime auditTime;  //审核时间

}