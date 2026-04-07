package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 经费申请表
 */
@Data
@TableName("expense_application")
public class ExpenseApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社团ID（关联club_info.id） */
    private Long clubId;

    /** 申请人ID（关联sys_user.id） */
    private Long applicantId;

    /** 申请金额（元） */
    private BigDecimal amount;

    /** 经费用途说明 */
    private String purpose;

    /** 关联活动ID（可选，关联activity_info.id） */
    private Long activityId;

    /** 证明材料路径（多个用逗号分隔或JSON） */
    private String attachments;

    /** 申请状态：0待审批，1已通过，2已驳回 */
    private Integer status;

    /** 申请人备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}