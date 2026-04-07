package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ExpenseApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 经费申请表 Mapper 接口
 */
@Mapper
public interface ExpenseApplicationMapper extends BaseMapper<ExpenseApplication> {
    // 如有复杂查询（如统计、联表），可在此添加自定义方法
}