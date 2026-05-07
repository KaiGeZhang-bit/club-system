package com.college.club.common.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 交易流水分页返回VO，包含统计数据
 * 继承通用分页VO，添加交易相关的统计字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionPageVO extends PageVO<ExpenseTransactionVO> {
    
    /**
     * 总收入（基于查询条件的全部数据）
     */
    private BigDecimal totalIncome;
    
    /**
     * 总支出（基于查询条件的全部数据）
     */
    private BigDecimal totalExpense;
    
    /**
     * 净收支 = 总收入 - 总支出
     */
    private BigDecimal netBalance;
}
