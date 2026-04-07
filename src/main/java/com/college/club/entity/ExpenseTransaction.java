package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 经费流水表
 */
@Data
@TableName("expense_transaction")
public class ExpenseTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社团ID（关联club_info.id） */
    private Long clubId;

    /** 交易类型：1收入，2支出 */
    private Integer type;

    /** 金额（元） */
    private BigDecimal amount;

    /** 来源/去向（如：学校拨款、赞助、活动支出） */
    private String source;

    /** 关联申请ID（支出时对应expense_application.id） */
    private Long applicationId;

    /** 关联活动ID（可选） */
    private Long activityId;

    /** 交易发生时间 */
    private LocalDateTime transactionTime;

    /** 详细描述 */
    private String description;

    /** 创建人ID（关联sys_user.id） */
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}