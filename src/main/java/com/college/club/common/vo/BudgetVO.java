package com.college.club.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetVO {
    private Long id;
    private Long clubId;
    private String clubName;
    private Integer year;
    private Integer quarter;
    private BigDecimal totalBudget;
    private BigDecimal usedBudget;
    private BigDecimal remainingBudget;
    private Double usageRate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
