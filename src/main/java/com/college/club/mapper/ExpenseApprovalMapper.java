package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ExpenseApproval;
import org.apache.ibatis.annotations.Mapper;

/**
 * 经费审批记录表 Mapper 接口
 */
@Mapper
public interface ExpenseApprovalMapper extends BaseMapper<ExpenseApproval> {
}