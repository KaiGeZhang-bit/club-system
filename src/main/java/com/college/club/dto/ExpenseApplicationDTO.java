package com.college.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "经费申请表单")
public class ExpenseApplicationDTO {

    @NotNull(message = "社团ID不能为空")
    @Schema(description = "社团ID")
    private Long clubId;

    @NotNull(message = "申请金额不能为空")
    @DecimalMin(value = "0.01", message = "申请金额必须大于0")
    @Schema(description = "申请金额（元）")
    private BigDecimal amount;

    @NotBlank(message = "经费用途不能为空")
    @Size(max = 500, message = "用途说明不能超过500字")
    @Schema(description = "经费用途说明")
    private String purpose;

    @Schema(description = "关联活动ID（可选）")
    private Long activityId;

    @Schema(description = "证明材料路径（多个用逗号分隔）")
    private String attachments;

    @Size(max = 500, message = "备注不能超过500字")
    @Schema(description = "申请人备注")
    private String remark;
}