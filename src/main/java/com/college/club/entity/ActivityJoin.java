package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_join") // 关键：绑定你的activity_join表，别改！
public class ActivityJoin {
    @TableId(type = IdType.AUTO) // 主键id自动增长
    private Long id;

    private Long activityId; // 对应表的activity_id字段
    private Long userId;     // 对应表的user_id字段
    private String userName; // 对应表的user_name字段
    private LocalDateTime joinTime; // 对应表的join_time字段
    private Integer signStatus;     // 对应表的sign_status字段（默认0未签到）
    private LocalDateTime signTime; // 对应表的sign_time字段
    @TableField("audit_status")
    private Integer auditStatus; // 审核状态：0待审核/1通过/2驳回

    @TableField("audit_time")
    private LocalDateTime auditTime; // 审核时间

    @TableField("auditor_id")
    private Long auditorId; // 审核人ID

    @TableField("audit_remark")
    private String auditRemark; // 审核备注

    @TableField("operate_user_id")
    private Long operateUserId;  //操作人ID

    @TableField("operate_user_name")
    private String operateUserName; //操作人姓名

}