package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.UserClubRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-社团关联Mapper接口
 */
@Mapper // 与你的ClubInfoMapper保持一致的注解风格
public interface UserClubRelationMapper extends BaseMapper<UserClubRelation> {
    /**
     * 查询指定社团的已加入成员ID（status=1）
     * @param clubId 社团ID
     * @return 成员ID列表
     */
    @Select("SELECT user_id FROM user_club_relation WHERE club_id = #{clubId} AND status = 1")
    List<Long> listJoinedMemberIds(@Param("clubId") Long clubId);

    /**
     * （可选）扩展：校验用户是否是该社团的已加入成员
     * @param userId 用户ID
     * @param clubId 社团ID
     * @return 1=是，0=否
     */
    @Select("SELECT COUNT(1) FROM user_club_relation WHERE user_id = #{userId} AND club_id = #{clubId} AND status = 1")
    Integer countMemberInClub(@Param("userId") Long userId, @Param("clubId") Long clubId);
}