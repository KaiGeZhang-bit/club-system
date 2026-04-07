package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ClubBudget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团预算表 Mapper 接口
 */
@Mapper
public interface ClubBudgetMapper extends BaseMapper<ClubBudget> {
}