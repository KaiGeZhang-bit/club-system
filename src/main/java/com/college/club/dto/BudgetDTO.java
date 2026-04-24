
package com.college.club.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetDTO {
    @NotNull(message = "社团ID不能为空")
    private Long clubId;

    @NotNull(message = "年份不能为空")
    private Integer year;

    private Integer quarter;

    @NotNull(message = "预算金额不能为空")
    @Positive(message = "预算金额必须大于0")
    private BigDecimal totalBudget;
}
