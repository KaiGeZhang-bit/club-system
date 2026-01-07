package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.UserClubRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-社团关联Mapper接口
 */
@Mapper // 与你的ClubInfoMapper保持一致的注解风格
public interface UserClubRelationMapper extends BaseMapper<UserClubRelation> {
}