package com.college.club.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseStatisticsVO {
    private Long clubId;
    private String clubName;
    
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    
    private BigDecimal budgetTotal;
    private BigDecimal budgetUsed;
    private Double budgetUsageRate;
    
    private Integer applicationCount;
    private Integer pendingApplicationCount;
}
