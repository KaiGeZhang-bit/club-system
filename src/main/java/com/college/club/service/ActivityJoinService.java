package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.ActivityJoinListVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ActivityJoinDTO;
import com.college.club.entity.ActivityJoin;

import java.util.List;

public interface ActivityJoinService extends IService<ActivityJoin> {
    //报名活动
    Result<?> joinActivity(ActivityJoinDTO dto);
    //取消报名
    Result<?> cancelJoin(ActivityJoinDTO dto);
    //查询指定活动的报名名单
    Result<List<ActivityJoinListVO>> getJoinListByActivityId(Long activityId);

    // 审核报名
    Result<?> auditActivityJoin(ActivityJoinDTO dto);
    // 查询单个用户的审核状态
    Result<ActivityJoinListVO> getAuditStatus(Long activityId, Long userId);
}