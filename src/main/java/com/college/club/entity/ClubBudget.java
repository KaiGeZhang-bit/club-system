package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社团预算表
 */
@Data
@TableName("club_budget")
public class ClubBudget {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社团ID */
    private Long clubId;

    /** 年份 */
    private Integer year;

    /** 季度（1-4），NULL表示年度总预算 */
    private Integer quarter;

    /** 总预算 */
    private BigDecimal totalBudget;

    /** 已使用预算 */
    private BigDecimal usedBudget;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}