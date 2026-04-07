package com.college.club.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "经费申请详情")
public class ExpenseApplicationVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "社团ID")
    private Long clubId;

    @Schema(description = "社团名称")
    private String clubName;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请金额")
    private BigDecimal amount;

    @Schema(description = "经费用途")
    private String purpose;

    @Schema(description = "关联活动ID")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "证明材料路径")
    private String attachments;

    @Schema(description = "申请状态：0待审批，1已通过，2已驳回")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "申请人备注")
    private String remark;

    @Schema(description = "审批记录")
    private ExpenseApprovalVO approval;

    @Schema(description = "关联流水（若已通过）")
    private ExpenseTransactionVO transaction;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}