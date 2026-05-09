package com.college.club.service;// UserClubRelationService.java
import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubJoinAuditDTO;
import com.college.club.dto.JoinClubDTO;
import com.college.club.entity.UserClubRelation;

public interface UserClubRelationService extends IService<UserClubRelation> {
    //加入社团
    Result<?> joinClub(JoinClubDTO dto, Long userId);
    //加入社团审核
    Result<String> auditJoinApply(ClubJoinAuditDTO dto);
    //查看我的加入社团申请
    Result<?> MyJoinApply(Integer pageNum, Integer pageSize);
    //撤回申请
    Result<?> withdraw(Long relationId);
    //管理员查看待审核加入申请列表
    Result<?> getPendingJoinApplyList(Integer pageNum, Integer pageSize, Long clubId);
}