package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.Result;
import com.college.club.dto.ActivityDTO;
import com.college.club.entity.ActivityInfo;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.mapper.ActivityInfoMapper;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.SysUserMapper;
import com.college.club.service.ActivityInfoService;
import com.college.club.common.vo.ActivityVO;
import com.college.club.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Resource
    private ClubInfoMapper clubInfoMapper;
    @Resource
    private SysUserService sysUserService;

    // 1. 创建活动（已实现）
    @Override
    public Result<ActivityVO> createActivity(ActivityDTO dto) {

        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId  = currentUser.getId();
        Long clubId = dto.getClubId();
        if(currentUser.getRole() != 1) {
            throw BusinessException.businessError("你不是社团负责人，无权进行该操作");
        }
        // 校验社团是否存在
        ClubInfo club = clubInfoMapper.selectById(clubId);
        if (club == null) {
            throw BusinessException.businessError("所属社团不存在");
        }
        if(!userId.equals(club.getLeaderId())){
            throw BusinessException.businessError("你不是该社团的负责人，无权对该社团进行操作");
        }
        // 构建活动实体
        ActivityInfo activity = new ActivityInfo();
        BeanUtils.copyProperties(dto, activity);
        activity.setClubName(club.getClubName());
        activity.setJoinNum(0); // 默认已报名人数0
        activity.setStatus(0);  // 默认状态0（待审核）
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        baseMapper.insert(activity);

        // 转VO返回
        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setStatusDesc(activity.getStatus() == 0 ? "待审核" : "已发布");
        return Result.success(vo);
    }

    // 2. 查询活动列表（补充实现）
    @Override
    public Result<List<ActivityVO>> getActivityList(Integer status, Long clubId) {
        QueryWrapper<ActivityInfo> query = new QueryWrapper<>();
        // 按状态筛选
        if (status != null) {
            query.eq("status", status);
        }
        // 按社团ID筛选
        if (clubId != null) {
            query.eq("club_id", clubId);
        }
        query.orderByDesc("create_time"); // 按创建时间倒序

        // 查询数据并转VO
        List<ActivityInfo> activityList = baseMapper.selectList(query);
        List<ActivityVO> voList = activityList.stream().map(activity -> {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(activity, vo);
            // 状态描述映射
            switch (activity.getStatus()) {
                case 0: vo.setStatusDesc("待审核"); break;
                case 1: vo.setStatusDesc("已发布"); break;
                case 2: vo.setStatusDesc("进行中"); break;
                case 3: vo.setStatusDesc("已结束"); break;
                default: vo.setStatusDesc("未知");
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    // 3. 查询活动详情（补充实现）
    @Override
    public Result<ActivityVO> getActivityById(Long id) {
        ActivityInfo activity = baseMapper.selectById(id);
        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }

        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);
        // 状态描述
        switch (activity.getStatus()) {
            case 0: vo.setStatusDesc("待审核"); break;
            case 1: vo.setStatusDesc("已发布"); break;
            case 2: vo.setStatusDesc("进行中"); break;
            case 3: vo.setStatusDesc("已结束"); break;
            default: vo.setStatusDesc("未知");
        }
        return Result.success(vo);
    }

    // 4. 更新活动状态（补充实现）
    @Override
    public Result<?> updateActivityStatus(Long id, Integer status) {


        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId  = currentUser.getId();

        ActivityInfo activity = baseMapper.selectById(id);

        // 老师审核
        if (currentUser.getRole() == 2) {
            // 只能处理待审核的活动，且只能改为1或4
            if (activity.getStatus() != 0) {
                throw BusinessException.businessError("只有待审核的活动才能审核");
            }
            if (status != 1 && status != 4) {
                throw BusinessException.businessError("老师只能将活动设为已发布(1)或已取消(4)");
            }
            activity.setStatus(status);
            activity.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(activity);
            return Result.success("审核成功");
        }



        // 校验状态是否合法
        if (!List.of(0, 1, 2, 3,4).contains(status)) {
            throw BusinessException.paramError("状态不合法（可选：0待审核/1已发布/2进行中/3已结束/已取消）");
        }


        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }
        Long clubId = activity.getClubId();
        ClubInfo club = clubInfoMapper.selectById(clubId);
        if(!userId.equals(club.getLeaderId())){
            throw BusinessException.businessError("你不是该社团的负责人，无权进行该操作");
        }



        // 更新状态和更新时间
        activity.setStatus(status);
        activity.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(activity);

        return Result.success("状态更新成功");
    }

    // 5. 删除活动（补充实现）
    @Override
    public Result<?> deleteActivity(Long id) {
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId  = currentUser.getId();


        ActivityInfo activity = baseMapper.selectById(id);
        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }
        ClubInfo club = clubInfoMapper.selectById(activity.getClubId());
        if(!userId.equals(club.getLeaderId())){
            throw BusinessException.businessError("你不是该社团负责人，无权进行该操作");
        }



        // 执行删除
        baseMapper.deleteById(id);


        return Result.success("活动删除成功");
    }

    //老师审核活动
    @Override
    public Result<?> auditActivity(Long id, Integer status) {
        // 获取当前登录用户，验证是否为老师
        SysUser currentUser = sysUserService.getCurrentUser();
        if (currentUser.getRole() != 2) {
            throw BusinessException.businessError("只有老师可以审核活动");
        }
        ActivityInfo activity = baseMapper.selectById(id);
        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }
        if (activity.getStatus() != 0) {
            throw BusinessException.businessError("只有待审核的活动才能被审核");
        }
        // 更新状态
        activity.setStatus(status);
        activity.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(activity);
        return Result.success("审核成功");
    }
}