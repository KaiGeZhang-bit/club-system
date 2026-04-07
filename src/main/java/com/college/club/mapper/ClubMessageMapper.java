package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ClubMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团消息表Mapper（club_message）
 * 核心：MyBatis-Plus自带批量插入，无需自定义SQL
 */
@Mapper
public interface ClubMessageMapper extends BaseMapper<ClubMessage> {
    // 无需自定义SQL：MyBatis-Plus的saveBatch()已封装批量插入逻辑
}