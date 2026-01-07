package com.college.club.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("club_quit_apply")
public class UserClubQuit {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;   //用户ID

    @TableField("club_id")
    private Long clubId;  //社团ID

    @TableField("apply_reason")
    private String  applyReason;  //退出社团理由

    @TableField("apply_time")
    private LocalDateTime applyTime;   //申请时间

    @TableField("audit_status")
    private Integer status; //只能为0，1，2

    @TableField("auditor_id")
    private Long auditorId;   //审核人ID

    @TableField("audit_time")
    private LocalDateTime auditTime;   //审核时间

    @TableField("audit_remark")
    private String  auditRemark;  //审核人驳回必填

}
