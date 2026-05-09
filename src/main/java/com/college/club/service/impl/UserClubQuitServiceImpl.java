package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.vo.MyQuitApplyVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.PendingQuitApplyVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubQuitAuditDTO; // 确保和你的DTO类名一致
import com.college.club.dto.QuitClubDTO;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.entity.UserClubQuit;
import com.college.club.entity.UserClubRelation;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.UserClubQuitMapper;
import com.college.club.mapper.UserClubRelationMapper;
import com.college.club.service.SysUserService;
import com.college.club.service.UserClubQuitService;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserClubQuitServiceImpl extends ServiceImpl<UserClubQuitMapper, UserClubQuit> implements UserClubQuitService {
    @Resource
    private UserClubQuitMapper userClubQuitMapper;
    @Resource ClubInfoMapper clubInfoMapper;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private UserClubRelationMapper userClubRelationMapper;
    @Override
    public Result<?> QuitClub(QuitClubDTO quitClubDTO) {
        SysUser currentUser = sysUserService.getCurrentUser(); //获取当前登录用户信息
        Long userId = currentUser.getId();  //获取当前登录用户的ID
        Long clubId = quitClubDTO.getClubId(); //获取用户要退出社团的ID
        String quitReason = quitClubDTO.getApplyReason(); //获取当前用户的退出理由

        //1.检验社团是否存在
        ClubInfo clubInfo = clubInfoMapper.selectById(clubId);
        if (clubInfo == null) {
            return Result.failBusiness("要退出的社团不存在");
        }

        //2.检验用户是否在该社团中
        QueryWrapper<UserClubRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("club_id", clubId);
        UserClubRelation userClubRelation = userClubRelationMapper.selectOne(queryWrapper);
        if (userClubRelation == null) {
            return Result.failBusiness("用户不在该社团中");
        }

        //3.检验该用户是否在推出社团的列表里，防止重复提交
        QueryWrapper<UserClubQuit> userClubQuitQueryWrapper = new QueryWrapper<>();
        userClubQuitQueryWrapper.eq("user_id", userId)
                .eq("club_id", clubId)
                .eq("audit_status", 0);

        UserClubQuit userClubQuit = this.userClubQuitMapper.selectOne(userClubQuitQueryWrapper);
        if (userClubQuit  != null) {
            return Result.failBusiness("请勿重复提交");
        }

        //将前端获取的内容填充到实体类中
        UserClubQuit userClubQuit1 = new UserClubQuit();
        userClubQuit1.setUserId(userId); //后端获取的用户Id
        userClubQuit1.setClubId(clubId);
        userClubQuit1.setApplyReason(quitReason);
        userClubQuit1.setApplyTime(LocalDateTime.now());
        userClubQuit1.setStatus(0);

        boolean saveSuccess = userClubQuitMapper.insert(userClubQuit1) > 0;
        if (!saveSuccess) {
            return Result.failBusiness("退出申请提交失败，请重新提交");
        }

        return Result.success("申请提交成功，请耐心等候审批完成");
    }


    @Override
    public Result<?> ClubQuitAudit(ClubQuitAuditDTO clubQuitAuditDTO) {
        //获取当前登录用户的信息
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        Integer roleId = currentUser.getRole();
        ClubInfo clubInfo = clubInfoMapper.selectById(clubQuitAuditDTO.getClubId());
        Long clubId = clubInfo.getId();


        //判断审核人身份
        if(roleId != 1){
            return Result.failBusiness("该用户不是社团负责人，无法进行该操作");
        }

        //判断要审核的申请是否存在
        UserClubQuit userClubQuit = getById(clubQuitAuditDTO.getId());
        if(userClubQuit == null){
            return Result.failBusiness("没有查询到要审核的申请");
        }

        //判断该社团是否有要处理的申请
        Long clubId1 = userClubQuit.getClubId();
        if(!Objects.equals(clubId, clubId1)){
            return Result.failBusiness("该社团没有要处理的申请");
        }


        //判断当前登录用户是否是该社团的负责人，防止越权审核
        if(!Objects.equals(userId, clubInfo.getLeaderId())){
            return Result.failBusiness("当前登录用户不是该社团的负责人，无权进行审核");
        }

        //判断该申请是否有被审核
        if(userClubQuit.getStatus() != 0){
            return Result.failBusiness("该申请已被审核，请勿重复审核");
        }

        //进行审核操作

        Integer status = clubQuitAuditDTO.getStatus();
        if(status == 1){
            userClubQuit.setStatus(1);
        } else if (status ==2) {
            userClubQuit.setStatus(2);

        }else{
            return Result.failBusiness("审核操作不合法");
        }

        //设置更新时间和审核人Id
        userClubQuit.setAuditTime(LocalDateTime.now());
        userClubQuit.setAuditorId(userId);

        //更新数据库
        boolean updateSuccess = userClubQuitMapper.updateById(userClubQuit) > 0;
        if (!updateSuccess) {
            return Result.failBusiness("更新失败，请重试!!!");
        }

        return Result.success("审核成功");
    }


    /**
     * 分页查询我的退出申请（适配你的实体类：status字段）
     */
    @Override
    public Result<?> getMyQuitApplyList(Integer pageNum, Integer pageSize) {
        // 处理分页参数默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // 获取当前登录用户ID
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();

        // 构造分页对象
        IPage<UserClubQuit> page = new Page<>(pageNum, pageSize);

        // 构造查询条件
        QueryWrapper<UserClubQuit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("apply_time");

        // 分页查询
        IPage<UserClubQuit> resultPage = this.baseMapper.selectPage(page, queryWrapper);
        List<UserClubQuit> quitApplyList = resultPage.getRecords();

        // 转换为VO列表
        List<MyQuitApplyVO> voList = new ArrayList<>();
        if (quitApplyList != null && !quitApplyList.isEmpty()) {
            for (UserClubQuit quitApply : quitApplyList) {
                MyQuitApplyVO vo = new MyQuitApplyVO();
                // 复制基础信息
                vo.setId(quitApply.getId());
                vo.setClubId(quitApply.getClubId());
                vo.setApplyReason(quitApply.getApplyReason());
                vo.setApplyTime(quitApply.getApplyTime());

                // 复制审核信息（关键修正：getStatus() 替代 getAuditStatus()）
                vo.setAuditStatus(quitApply.getStatus()); // 实体字段是status，赋值给VO的auditStatus
                vo.setAuditRemark(quitApply.getAuditRemark());
                vo.setAuditTime(quitApply.getAuditTime());
                vo.setAuditorId(quitApply.getAuditorId());

                // 关联查询社团名称
                ClubInfo clubInfo = clubInfoMapper.selectById(quitApply.getClubId());
                if (clubInfo != null) {
                    vo.setClubName(clubInfo.getClubName());
                } else {
                    vo.setClubName("该社团已删除");
                }

                // 转换审核状态为文字描述
                Integer status = quitApply.getStatus(); // 修正：使用你的实体字段status
                if (status == 0) {
                    vo.setAuditStatusDesc("待审核");
                } else if (status == 1) {
                    vo.setAuditStatusDesc("通过");
                } else if (status == 2) {
                    vo.setAuditStatusDesc("驳回");
                } else {
                    vo.setAuditStatusDesc("未知状态");
                }

                voList.add(vo);
            }
        }

        // 封装分页VO
        PageVO<MyQuitApplyVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPages(resultPage.getPages());
        pageVO.setCurrent(pageNum);
        pageVO.setSize(pageSize);

        return Result.success(pageVO);
    }
    /**
     * 撤销我的推出社团申请

     */

    @Override
    public Result<?> withdrawQuitApply(Long QuitApplyId) {

        //获取当前用户登录信息
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();

        //判断要撤销的申请是否存在
        UserClubQuit userClubQuit = this.baseMapper.selectById(QuitApplyId);
        if(userClubQuit == null){
            return Result.failBusiness("要撤销的申请不存在");
        }

        //判断要撤销的申请是否是自己的
        if(!userClubQuit.getUserId().equals(userId)){
            return Result.failBusiness("撤销的申请不是自己的");
        }

        //只能撤销没有被审核的申请

        if(userClubQuit.getStatus() == 1){
            return Result.failBusiness("该申请已被审核通过，无法撤销");
        }

        //执行撤销动作

        userClubQuit.setStatus(3);
        this.baseMapper.updateById(userClubQuit);



        return Result.success("撤销成功");
    }

    /**
     * 管理员查看待审核退出申请列表
     * 仅社团负责人（role=1）和老师（role=2）可以查看
     */
    @Override
    public Result<?> getPendingQuitApplyList(Integer pageNum, Integer pageSize, Long clubId) {
        // 处理分页参数默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // 获取当前登录用户信息
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        Integer userRole = currentUser.getRole();

        // 权限校验：仅社团负责人和老师可以查看
        if (userRole != 1 && userRole != 2) {
            throw new RuntimeException("仅社团负责人和老师可以查看待审核申请列表");
        }

        // 构造分页对象
        IPage<UserClubQuit> page = new Page<>(pageNum, pageSize);

        // 构造查询条件：只查询待审核状态（status=0）的申请
        QueryWrapper<UserClubQuit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", 0); // 只查待审核的

        // 如果是社团负责人，只能查看自己负责的社团的申请
        if (userRole == 1) {
            // 查询该用户作为负责人的社团ID列表
            QueryWrapper<ClubInfo> clubQuery = new QueryWrapper<>();
            clubQuery.eq("leader_id", userId);
            List<ClubInfo> managedClubs = clubInfoMapper.selectList(clubQuery);

            if (managedClubs == null || managedClubs.isEmpty()) {
                // 如果没有负责的社团，返回空列表
                PageVO<PendingQuitApplyVO> pageVO = new PageVO<>();
                pageVO.setRecords(new ArrayList<>());
                pageVO.setTotal(0L);
                pageVO.setPages(0L);
                pageVO.setCurrent(pageNum);
                pageVO.setSize(pageSize);
                return Result.success(pageVO);
            }

            List<Long> clubIds = managedClubs.stream()
                    .map(ClubInfo::getId)
                    .toList();
            queryWrapper.in("club_id", clubIds);
        }

        // 如果指定了社团ID，进一步过滤
        if (clubId != null) {
            queryWrapper.eq("club_id", clubId);
        }

        // 按申请时间降序排列
        queryWrapper.orderByDesc("apply_time");

        // 分页查询
        IPage<UserClubQuit> resultPage = this.baseMapper.selectPage(page, queryWrapper);
        List<UserClubQuit> pendingList = resultPage.getRecords();

        // 转换为VO列表
        List<PendingQuitApplyVO> voList = new ArrayList<>();
        if (pendingList != null && !pendingList.isEmpty()) {
            for (UserClubQuit quitApply : pendingList) {
                PendingQuitApplyVO vo = new PendingQuitApplyVO();
                vo.setId(quitApply.getId());
                vo.setUserId(quitApply.getUserId());
                vo.setClubId(quitApply.getClubId());
                vo.setApplyReason(quitApply.getApplyReason());
                vo.setApplyTime(quitApply.getApplyTime());
                vo.setStatus(quitApply.getStatus());
                vo.setStatusDesc("待审核");

                // 关联查询用户姓名
                SysUser user = sysUserService.getById(quitApply.getUserId());
                if (user != null) {
                    vo.setUserName(user.getUsername());
                } else {
                    vo.setUserName("未知用户");
                }

                // 关联查询社团名称
                ClubInfo clubInfo = clubInfoMapper.selectById(quitApply.getClubId());
                if (clubInfo != null) {
                    vo.setClubName(clubInfo.getClubName());
                } else {
                    vo.setClubName("该社团已删除");
                }

                voList.add(vo);
            }
        }

        // 封装分页VO
        PageVO<PendingQuitApplyVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPages(resultPage.getPages());
        pageVO.setCurrent(pageNum);
        pageVO.setSize(pageSize);

        return Result.success(pageVO);
    }
}
