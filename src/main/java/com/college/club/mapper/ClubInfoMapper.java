package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ClubInfo;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

public interface ClubInfoMapper extends BaseMapper<ClubInfo> {

    @Update("UPDATE club_info SET status = #{status}, auditor_id = #{auditorId}, " +
            "audit_remark = #{auditRemark}, audit_time = #{auditTime}, " +
            "update_time = #{updateTime} WHERE id = #{clubId}")
    int updateClubAuditInfo(
            @Param("clubId") Long clubId,
            @Param("status") Integer status,
            @Param("auditorId") Long auditorId,
            @Param("auditRemark") String auditRemark,
            @Param("auditTime") LocalDateTime auditTime,
            @Param("updateTime") LocalDateTime updateTime
    );
}