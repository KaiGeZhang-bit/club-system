package com.college.club.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "经费流水")
public class ExpenseTransactionVO {

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "社团ID")
    private Long clubId;

    @Schema(description = "社团名称")
    private String clubName;

    @Schema(description = "交易类型：1收入，2支出")
    private Integer type;

    @Schema(description = "类型文本")
    private String typeText;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "来源/去向")
    private String source;

    @Schema(description = "关联申请ID")
    private Long applicationId;

    @Schema(description = "关联活动ID")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "交易时间")
    private LocalDateTime transactionTime;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建人姓名")
    private String createByName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}