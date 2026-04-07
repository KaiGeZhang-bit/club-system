package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ExpenseTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 经费流水表 Mapper 接口
 */
@Mapper
public interface ExpenseTransactionMapper extends BaseMapper<ExpenseTransaction> {
}