package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.MyJoinApplyVO;
import com.college.club.common.vo.MyQuitApplyVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubJoinAuditDTO;
import com.college.club.dto.JoinClubDTO;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.entity.UserClubQuit;
import com.college.club.entity.UserClubRelation;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.SysUserMapper;
import com.college.club.mapper.UserClubRelationMapper;
import com.college.club.service.ClubInfoService;
import com.college.club.service.SysUserService;
import com.college.club.service.UserClubRelationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service // 告诉代码：这是处理社团业务逻辑的类
public class UserClubRelationServiceImpl extends ServiceImpl<UserClubRelationMapper, UserClubRelation> implements UserClubRelationService {

    // 找到“社团表的管理员”，用来查社团信息
    @Resource
    private ClubInfoMapper clubInfoMapper;
    @Resource
    private SysUserService sysUserService;

    // 加入社团的核心逻辑（移除人数相关所有逻辑）
    @Override
    public Result<?> joinClub(JoinClubDTO dto, Long userId) {
        // 1. 查社团是否存在
        ClubInfo club = clubInfoMapper.selectById(dto.getClubId());
        if (club == null) {
            throw BusinessException.businessError("社团不存在");
        }
        // 2. 只有“正常（招新中）”的社团能加入（状态1=正常）
        if (club.getStatus() != 1) {
            throw BusinessException.businessError("只有正常招新中的社团才能加入");
        }
        // 3. 查是否重复加入/重复申请（同一个用户不能重复操作）
        QueryWrapper<UserClubRelation> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("club_id", dto.getClubId());
        UserClubRelation existingRelation = baseMapper.selectOne(query);
        if (existingRelation != null) {
            // 已加入（1）提示重复，待审核（0）提示申请已提交
            if (existingRelation.getStatus() == 1) {
                throw BusinessException.businessError("你已加入该社团，无需重复申请");
            } else {
                throw BusinessException.businessError("你已提交该社团加入申请，无需重复操作");
            }
        }
        // 4. 保存加入社团记录到user_club_relation表（仅保留核心记录逻辑）
        UserClubRelation relation = new UserClubRelation();
        relation.setUserId(userId);
        BeanUtils.copyProperties(dto, relation); // 复制DTO字段到实体
        relation.setJoinTime(LocalDateTime.now()); // 加入时间=现在
        relation.setStatus(0); // 加入状态默认0（待审核）
        baseMapper.insert(relation);

        return Result.success("加入社团申请已提交");
    }


    @Override
    public Result<String> auditJoinApply(ClubJoinAuditDTO dto) {

        //1.获取当前登录用户（从session中获取，为获取会抛出业务异常）
        SysUser currentUser = sysUserService.getCurrentUser();
        //2。校验当前用户是社团负责人（role =1 )->业务错误
        if (currentUser.getRole() != 1) {
            throw BusinessException.businessError("仅社团负责人可执行审核操作");
        }
        //3.根据申请ID查询申请记录 -> 业务错误
        UserClubRelation apply = this.getById(dto.getApplyId());
        if (apply == null) {
            throw BusinessException.businessError("加入社团申请记录不存在");
        }
        //4.查询申请对应的社团，校验当前用户是该社团负责人 ->业务错误
        ClubInfo club = clubInfoMapper.selectById(apply.getClubId());
        if (club == null) {
            throw BusinessException.businessError("申请对应的社团不存在");
        }
        if (!currentUser.getId().equals(club.getLeaderId())) {
            throw BusinessException.businessError("你不是该社团的负责人，无权进行审核");
        }
        //5.校验申请是带审核状态（status = 0）->业务错误
        if (apply.getStatus() != 0) {
            throw BusinessException.businessError("该申请已审核，无需重复审核");
        }
        //6.处理审核操作
        Integer auditAction = dto.getAuditAction();
        if (auditAction == 1) {
            apply.setStatus(1);
        } else if (auditAction == 2) {
            apply.setStatus(2);
        } else {
            return (Result<String>) Result.failParam("审核操作不合法");
        }

        //7。设置审核时间和更新时间
        apply.setAuditTime(LocalDateTime.now());
        apply.setJoinTime(LocalDateTime.now());
        //8.保存更新到数据库 ->系统错误
        boolean updateSuccess = updateById(apply);
        if (!updateSuccess) {
            return (Result<String>) Result.failSystem("审核操作失败，请重试！！！");
        }

        //9.返回审核结果 -> 成功，将提示信息作为data传递
        String msg = auditAction == 1 ? "申请审核通过，用户已经加入社团" : "申请已驳回";
        return Result.success(msg);

    }

    /**
     * 分页查询我的加入社团申请
     */

    @Override
    public Result<?> MyJoinApply(Integer pageNum, Integer pageSize) {

        //给分页参数设定默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }


        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();

        IPage<UserClubRelation> page = new Page<>(pageNum, pageSize);

        QueryWrapper<UserClubRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("join_time");


        IPage<UserClubRelation> resultPage = this.baseMapper.selectPage(page, queryWrapper);
        List<UserClubRelation> JoinApplyList = resultPage.getRecords();


        List<MyJoinApplyVO> voList = new ArrayList<>();
        if (JoinApplyList != null && !JoinApplyList.isEmpty()) {
            for (UserClubRelation JoinApp : JoinApplyList) {

                MyJoinApplyVO vo = new MyJoinApplyVO();
                vo.setId(JoinApp.getId());
                vo.setUserId(JoinApp.getUserId());
                vo.setClubId(JoinApp.getClubId());
                vo.setJoinTime(JoinApp.getJoinTime());
                vo.setStatus(JoinApp.getStatus());

                // 2. 关联查询社团名称（赋值给字符串字段clubName）
                ClubInfo clubInfo = clubInfoMapper.selectById(JoinApp.getClubId());
                if (clubInfo != null) {
                    vo.setClubName(clubInfo.getClubName()); // ✅ 字符串赋值给字符串字段
                } else {
                    vo.setClubName("该社团已删除"); // ✅ 字符串赋值给字符串字段
                }

                // 3. 转换审核状态为文字描述（赋值给字符串字段statusDesc）
                Integer status = JoinApp.getStatus();
                if (status == 0) {
                    vo.setStatusDesc("待审核"); // ✅ 文字赋值给字符串描述字段
                } else if (status == 1) {
                    vo.setStatusDesc("通过");
                } else if (status == 2) {
                    vo.setStatusDesc("驳回");
                } else {
                    vo.setStatusDesc("未知状态");
                }
                voList.add(vo);


            }
        }


        // 封装分页VO
        PageVO<MyJoinApplyVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPages(resultPage.getPages());
        pageVO.setCurrent(pageNum);
        pageVO.setSize(pageSize);

        return Result.success(pageVO);
    }


    /**
     * 撤销申请
     */
    @Override
    public Result<?> withdraw(Long relationId) {

        try {
            //获取当前登录用户身份信息
            SysUser currentUser = sysUserService.getCurrentUser();
            Long userId = currentUser.getId();

            //判断要撤销的申请是否存在
            UserClubRelation relation = this.baseMapper.selectById(relationId);
            if (relation == null) {
                return Result.failBusiness("要撤销的申请不存在");
            }

            //判断要撤销的申请是否是自己的
            if (!currentUser.getId().equals(relation.getUserId())) {
                return Result.failBusiness("你只能撤销自己的申请");
            }

            //用户只能撤回处于待审核和审核不通过的申请
            if (relation.getStatus() == 1) {
                return Result.failBusiness("该申请已申请通过，无法撤回");
            }

            //执行撤销操作

            //操作1：直接删除
            this.baseMapper.deleteById(relationId);
            //操作2：留下痕迹
//            relation.setStatus(4);
//            this.baseMapper.updateById(relation);


            return Result.success("成功撤销");
        }catch (Exception e) {
            log.error("撤回申请失败",e);  //注入log
            return Result.failBusiness("撤销失败，请重试");
        }
    }

}