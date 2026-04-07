package com.college.club.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "审批记录")
public class ExpenseApprovalVO {

    @Schema(description = "审批记录ID")
    private Long id;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批动作：1通过，2驳回")
    private Integer action;

    @Schema(description = "审批意见")
    private String remark;

    @Schema(description = "审批时间")
    private LocalDateTime createTime;
}