package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 经费审批记录表
 */
@Data
@TableName("expense_approval")
public class ExpenseApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请ID（关联expense_application.id） */
    private Long applicationId;

    /** 审批人ID（关联sys_user.id） */
    private Long approverId;

    /** 审批动作：1通过，2驳回 */
    private Integer action;

    /** 审批意见 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}