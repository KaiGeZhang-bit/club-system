package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ActivityJoinListVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ActivityJoinDTO;
import com.college.club.entity.ActivityInfo;
import com.college.club.entity.ActivityJoin;
import com.college.club.mapper.ActivityInfoMapper;
import com.college.club.mapper.ActivityJoinMapper;
import com.college.club.service.ActivityJoinService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service // 告诉代码：这是处理业务逻辑的类
public class ActivityJoinServiceImpl extends ServiceImpl<ActivityJoinMapper, ActivityJoin> implements ActivityJoinService {

    // 找到“活动表的管理员”，用来查活动信息、改报名人数
    @Resource
    private ActivityInfoMapper activityInfoMapper;

    // 报名活动的核心逻辑
    @Override
    public Result<?> joinActivity(ActivityJoinDTO dto) {
        // 1. 查活动是否存在
        ActivityInfo activity = activityInfoMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }
        // 2. 只有“已发布/进行中”的活动能报名（状态1=已发布，2=进行中）
        if (activity.getStatus() != 1 && activity.getStatus() != 2) {
            throw BusinessException.businessError("只有已发布/进行中的活动才能报名");
        }
        // 3. 查是否重复报名（同一个用户不能报同一个活动）
        QueryWrapper<ActivityJoin> query = new QueryWrapper<>();
        query.eq("activity_id", dto.getActivityId()).eq("user_id", dto.getUserId());
        ActivityJoin existingJoin = baseMapper.selectOne(query);
        if (existingJoin != null) {
            throw BusinessException.businessError("你已报名该活动，无需重复报名");
        }
        // 4. 查报名人数是否已满
        if (activity.getJoinNum() >= activity.getMaxNum()) {
            throw BusinessException.businessError("活动报名人数已满，无法报名");
        }
        // 5. 保存报名记录到你的activity_join表
        ActivityJoin join = new ActivityJoin();
        BeanUtils.copyProperties(dto, join);
        join.setJoinTime(LocalDateTime.now()); // 报名时间=现在
        join.setSignStatus(0); // 签到状态默认0（未签到）
        baseMapper.insert(join);
        // 6. 活动已报名人数+1
        activity.setJoinNum(activity.getJoinNum() + 1);
        activity.setUpdateTime(LocalDateTime.now());
        activityInfoMapper.updateById(activity);

        return Result.success("报名成功");
    }

    //取消报名的核心逻辑
    @Override
    public Result<?> cancelJoin(ActivityJoinDTO dto) {
        //1.是否有报名记录
        QueryWrapper<ActivityJoin> query = new QueryWrapper<>();
        query.eq("activity_id", dto.getActivityId()).eq("user_id", dto.getUserId());
        ActivityJoin existingJoin = baseMapper.selectOne(query);
        if (existingJoin == null) {
            throw BusinessException.businessError("你未报名该活动，无法取消");
        }
        ActivityInfo activity = activityInfoMapper.selectById(dto.getActivityId());
        if (activity.getStatus() != 1 && activity.getStatus() != 2) {
            throw BusinessException.businessError("活动已结束，无法取消报名");
        }
        baseMapper.delete(query);
        activity.setJoinNum(activity.getJoinNum() - 1);
        activity.setUpdateTime(LocalDateTime.now());
        activityInfoMapper.updateById(activity);

        return Result.success("取消报名成功");
    }


    @Override
    public Result<List<ActivityJoinListVO>> getJoinListByActivityId(Long activityId) {

        //检验活动是否存在
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }

        //查询该活动的所有报名记录
        QueryWrapper<ActivityJoin> query = new QueryWrapper<>();
        query.eq("activity_id", activityId);
        List<ActivityJoin> joinList = baseMapper.selectList(query);

        //把数据库记录转成用户可以看懂的VO列表
        List<ActivityJoinListVO> voList = joinList.stream().map(join -> {
            ActivityJoinListVO vo = new ActivityJoinListVO();
            vo.setUserId(join.getUserId());
            vo.setUserName(join.getUserName());
            vo.setJoinTime(join.getJoinTime());
            //签到状态数字转文字
            vo.setSignStatusDesc(join.getSignStatus() == 0 ? "未签到" : "已签到");
            return vo;
        }).toList();
        return Result.success(voList);


    }


    // 4. 审核报名（匹配你的Result方法）
    @Override
    public Result<?> auditActivityJoin(ActivityJoinDTO dto) {
        // 校验审核状态（参数错误）
        if (dto.getAuditStatus() == null || (dto.getAuditStatus() != 1 && dto.getAuditStatus() != 2)) {
            return Result.failParam("审核状态只能为1（通过）或2（驳回）");
        }

        // 校验报名记录存在（业务错误）
        QueryWrapper<ActivityJoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", dto.getActivityId())
                .eq("user_id", dto.getUserId());
        ActivityJoin activityJoin = this.getOne(queryWrapper);
        if (activityJoin == null) {
            return Result.failBusiness("该用户未报名此活动，无法审核");
        }

        // 校验是否已审核（业务错误）
        if (activityJoin.getAuditStatus() != 0) {
            String status = activityJoin.getAuditStatus() == 1 ? "审核通过" : "审核驳回";
            return Result.failBusiness("该记录已审核（状态：" + status + "），不可重复操作");
        }

        // 驳回时校验备注（参数错误）
        if (dto.getAuditStatus() == 2) {
            if (dto.getAuditRemark() == null || dto.getAuditRemark().trim().length() == 0) {
                return Result.failParam("驳回报名必须填写审核备注");
            }
        }

        // 更新审核信息
        activityJoin.setAuditStatus(dto.getAuditStatus());
        activityJoin.setAuditorId(dto.getAuditorId());
        activityJoin.setAuditRemark(dto.getAuditRemark());
        activityJoin.setAuditTime(LocalDateTime.now());
        this.updateById(activityJoin);

        return Result.success("审核成功");
    }

    // 5. 查询单个用户的审核状态（匹配你的Result方法）
    @Override
    public Result<ActivityJoinListVO> getAuditStatus(Long activityId, Long userId) {
        QueryWrapper<ActivityJoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", activityId)
                .eq("user_id", userId);
        ActivityJoin activityJoin = this.getOne(queryWrapper);
        if (activityJoin == null) {
            return (Result<ActivityJoinListVO>) Result.failBusiness("该用户未报名此活动");
        }

        ActivityJoinListVO vo = new ActivityJoinListVO();
        BeanUtils.copyProperties(activityJoin, vo);
        vo.setSignStatusDesc(activityJoin.getSignStatus() == 1 ? "已签到" : "未签到");

        switch (activityJoin.getAuditStatus()) {
            case 0:
                vo.setAuditStatusName("待审核");
                break;
            case 1:
                vo.setAuditStatusName("审核通过");
                break;
            case 2:
                vo.setAuditStatusName("审核驳回");
                break;
            default:
                vo.setAuditStatusName("未知状态");
        }

        return Result.success(vo);
    }

}