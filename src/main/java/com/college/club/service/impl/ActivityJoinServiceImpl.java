package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ActivityJoinListVO;
import com.college.club.common.vo.ActivitySignQrVo;
import com.college.club.common.vo.Result;
import com.college.club.dto.ActivityJoinDTO;
import com.college.club.dto.ScanSignReqDTO;
import com.college.club.entity.ActivityInfo;
import com.college.club.entity.ActivityJoin;
import com.college.club.entity.SysUser;
import com.college.club.mapper.ActivityInfoMapper;
import com.college.club.mapper.ActivityJoinMapper;
import com.college.club.service.ActivityJoinService;
import com.college.club.service.SysUserService;
import com.college.club.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service // 告诉代码：这是处理业务逻辑的类

@Slf4j

@RequiredArgsConstructor

public class ActivityJoinServiceImpl extends ServiceImpl<ActivityJoinMapper, ActivityJoin> implements ActivityJoinService {

    // 找到“活动表的管理员”，用来查活动信息、改报名人数
    @Resource
    private ActivityInfoMapper activityInfoMapper;

    @Resource
    private ActivityJoinMapper activityJoinMapper;

    @Resource
    private SysUserService sysUserService;

    //二维码业务前缀（区分本系统签到二维码，防止篡改）
    private static final String QR_CONTENT_PREFIX = "club_activity_sign_";
    //局域网IP
    private static final String LOCAL_IP = "192.168.0.8";


    // 报名活动的核心逻辑
    @Override
    public Result<?> joinActivity(Long activityId,Long userId) {
        // 1. 查活动是否存在
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null) {
            throw BusinessException.businessError("活动不存在");
        }
        // 2. 只有“已发布/进行中”的活动能报名（状态1=已发布，2=进行中）
        if (activity.getStatus() != 1 && activity.getStatus() != 2) {
            throw BusinessException.businessError("只有已发布/进行中的活动才能报名");
        }
        // 3. 查是否重复报名（同一个用户不能报同一个活动）
        QueryWrapper<ActivityJoin> query = new QueryWrapper<>();
        query.eq("activity_id", activityId).eq("user_id", userId);
        ActivityJoin existingJoin = baseMapper.selectOne(query);
        if (existingJoin != null) {
            throw BusinessException.businessError("你已报名该活动，无需重复报名");
        }
        // 4. 查报名人数是否已满
        if (activity.getJoinNum() >= activity.getMaxNum()) {
            throw BusinessException.businessError("活动报名人数已满，无法报名");
        }
        //4.获取报名用户真实姓名
        SysUser user = sysUserService.getById(userId);
        String username = user != null ? user.getUsername() : null;

        ActivityJoin join = new ActivityJoin();
        // 5. 保存报名记录到你的activity_join表
        join.setActivityId(activityId);
        join.setUserId(userId);
        join.setUserName(username);
        join.setJoinTime(LocalDateTime.now());    // 报名时间
        join.setSignStatus(0);                    // 未签到
        // ====================== 修复完成 ======================

        baseMapper.insert(join);

        // 6. 活动已报名人数+1
        activity.setJoinNum(activity.getJoinNum() + 1);
        activity.setUpdateTime(LocalDateTime.now());
        activityInfoMapper.updateById(activity);

        return Result.success("报名成功");
    }

    //取消报名的核心逻辑
    @Override
    public Result<?> cancelJoin(Long activityId,Long userId) {
        //1.是否有报名记录
        QueryWrapper<ActivityJoin> query = new QueryWrapper<>();
        query.eq("activity_id", activityId).eq("user_id", userId);
        ActivityJoin existingJoin = baseMapper.selectOne(query);
        if (existingJoin == null) {
            throw BusinessException.businessError("你未报名该活动，无法取消");
        }
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
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

        SysUser currentUser = sysUserService.getCurrentUser();
        if (currentUser == null) {
            return Result.failBusiness("请先登录");
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
        activityJoin.setAuditorId(currentUser.getId());
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

    @Override
    public Result<ActivitySignQrVo> generateSignQr(Long activityId) {
        //1.基础参数校验，活动ID必须为整数
        if(activityId == null || activityId <= 0) {
            // 直接new Result对象，绕过泛型转换
            return new Result<>(403, "活动ID不存在且活动ID必须为整数", null);
        }

        //2.校验活动是否有报名记录（无报名记录无法生成二维码）
        LambdaQueryWrapper<ActivityJoin> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(ActivityJoin::getActivityId, activityId);
        Long joinCount = this.count(countWrapper);
        if (joinCount == 0) {
            // 直接new Result对象
            return new Result<>(403, "该活动没有报名人员，无法生成签到二维码", null);
        }
        try{
            //3.构造二维码业务内容（前缀+活动ID，做基础业务区分）

            String qrContent = QR_CONTENT_PREFIX + activityId;

            //4，复用工具类生成二维码Base64（没有额外要求，默认使用宽高300*300）
            String qrCodeBase64 = QrCodeUtil.generateQrCodeBase64(qrContent);

            //5.封装返回结果VO
            ActivitySignQrVo qrVo = new ActivitySignQrVo();
            qrVo.setActivityId(activityId);
            qrVo.setQrContent(qrContent);
            qrVo.setQrCodeBase64(qrCodeBase64);

            log.info("活动{}的签到二维码生成成功，当前报名人数：{}", activityId, joinCount);
            return Result.success(qrVo); // 成功分支不动，这里是对的
        }catch (RuntimeException e) {
            log.error("生成活动{}的签到二维码失败", activityId, e);
            // 直接new Result对象
            return new Result<>(403, "生成二维码失败，请重新核实条件", null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> scanSign(ScanSignReqDTO reqDTO,Long userId) {
        // 1. 基础参数校验
        if (reqDTO == null || !StringUtils.hasText(reqDTO.getQrContent()) || userId== null || userId <= 0) {
            return Result.failBusiness("签到参数不完整");
        }

        // 2. 提取参数
        String qrContent = reqDTO.getQrContent();
        String signIp = StringUtils.hasText(reqDTO.getSignIp()) ? reqDTO.getSignIp() : "未知IP";

        // 3. 解析二维码内容（完全适配你的"前缀+活动ID"逻辑）
        Long activityId = null;
        // 校验前缀是否匹配
        if (!qrContent.startsWith(QR_CONTENT_PREFIX)) {
            return Result.failBusiness("无效的签到二维码");
        }
        try {
            // 直接截取前缀后的部分作为活动ID（和你生成逻辑完全对应）
            String activityIdStr = qrContent.replace(QR_CONTENT_PREFIX, "");
            activityId = Long.parseLong(activityIdStr);
        } catch (NumberFormatException e) {
            log.error("解析二维码内容失败，内容：{}", qrContent, e);
            return Result.failBusiness("二维码内容损坏，无法解析活动ID");
        }

        // 4. 查询用户报名记录
        LambdaQueryWrapper<ActivityJoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJoin::getActivityId, activityId)
                .eq(ActivityJoin::getUserId, userId);
        ActivityJoin joinRecord = activityJoinMapper.selectOne(queryWrapper);

        // 5. 核心业务校验
        // 5.1 校验是否报名
        if (joinRecord == null) {
            return Result.failBusiness("你未报名该活动，无法签到");
        }
        // 5.2 校验审核状态（1=审核通过）
        if (joinRecord.getAuditStatus() == null || joinRecord.getAuditStatus() != 1) {
            return Result.failBusiness("报名审核未通过，无法签到");
        }
        // 5.3 校验是否已签到（1=已签到）
        if (joinRecord.getSignStatus() != null && joinRecord.getSignStatus() == 1) {
            return Result.failBusiness("你已完成签到，请勿重复操作");
        }

        // 6. 执行签到：更新报名记录的签到状态和时间
        LambdaUpdateWrapper<ActivityJoin> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityJoin::getId, joinRecord.getId())
                .set(ActivityJoin::getSignStatus, 1)          // 标记为已签到
                .set(ActivityJoin::getSignTime, LocalDateTime.now()); // 记录签到时间

        int updateCount = activityJoinMapper.update(null, updateWrapper);
        if (updateCount <= 0) {
            log.error("用户{}签到活动{}失败，更新记录数为0", userId, activityId);
            return Result.failBusiness("签到失败，请重试");
        }

        // 7. 日志+返回结果
        log.info("用户{}成功签到活动{}，签到IP：{}", userId, activityId, signIp);
        return Result.success("签到成功！");
    }

}