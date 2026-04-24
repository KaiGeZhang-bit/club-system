package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ExpenseApplicationVO;
import com.college.club.common.vo.ExpenseApprovalVO;
import com.college.club.common.vo.ExpenseTransactionVO;
import com.college.club.common.vo.PageVO;
import com.college.club.dto.ExpenseApplicationDTO;
import com.college.club.entity.*;
import com.college.club.mapper.ExpenseApplicationMapper;
import com.college.club.mapper.ExpenseApprovalMapper;
import com.college.club.service.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseApplicationServiceImpl extends ServiceImpl<ExpenseApplicationMapper, ExpenseApplication>
        implements ExpenseApplicationService {

    @Autowired
    private ClubInfoService clubInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ActivityInfoService activityInfoService;
    @Autowired
    private ExpenseApprovalMapper expenseApprovalMapper;

    @Autowired
    private ExpenseTransactionService transactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApplication(ExpenseApplicationDTO dto, Long applicantId) {
        ClubInfo club = clubInfoService.getById(dto.getClubId());
        if (club == null) throw BusinessException.businessError("社团不存在");

        boolean isLeader = club.getLeaderId() != null && club.getLeaderId().equals(applicantId);
        boolean isTeacher = club.getTeacherId() != null && club.getTeacherId().equals(applicantId);
        if (!isLeader && !isTeacher) throw BusinessException.businessError("无权限提交该社团的经费申请");

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw BusinessException.paramError("申请金额必须大于0");

        ExpenseApplication application = new ExpenseApplication();
        BeanUtils.copyProperties(dto, application);
        application.setApplicantId(applicantId);
        application.setStatus(0);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        this.save(application);
        return application.getId();
    }

    @Override
    public PageVO<ExpenseApplicationVO> queryApplicationList(Integer pageNum, Integer pageSize,
                                                             Long clubId, Integer status,
                                                             LocalDateTime startTime, LocalDateTime endTime) {
        SysUser currentUser = sysUserService.getCurrentUser();

        // 社团负责人只能查看自己管理的社团申请
        if (currentUser.getRole() == 1 && clubId == null) {
            ClubInfo myClub = clubInfoService.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClubInfo>()
                            .eq(ClubInfo::getLeaderId, currentUser.getId())
            );
            if (myClub == null) {
                throw BusinessException.businessError("您还不是任何社团的负责人");
            }
            clubId = myClub.getId();
        }

        LambdaQueryWrapper<ExpenseApplication> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(ExpenseApplication::getClubId, clubId);
        if (status != null) wrapper.eq(ExpenseApplication::getStatus, status);
        if (startTime != null) wrapper.ge(ExpenseApplication::getCreateTime, startTime);
        if (endTime != null) wrapper.le(ExpenseApplication::getCreateTime, endTime);
        wrapper.orderByDesc(ExpenseApplication::getCreateTime);

        Page<ExpenseApplication> page = new Page<>(pageNum, pageSize);
        Page<ExpenseApplication> resultPage = this.page(page, wrapper);

        List<ExpenseApplicationVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageVO<ExpenseApplicationVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPages(resultPage.getPages());
        pageVO.setCurrent((int) resultPage.getCurrent());
        pageVO.setSize((int) resultPage.getSize());
        return pageVO;
    }

    @Override
    public ExpenseApplicationVO getApplicationDetail(Long applicationId) {
        ExpenseApplication application = this.getById(applicationId);
        if (application == null) throw BusinessException.businessError("申请不存在");
        ExpenseApplicationVO vo = convertToVO(application);

        ExpenseApproval approval = expenseApprovalMapper.selectOne(
                new LambdaQueryWrapper<ExpenseApproval>()
                        .eq(ExpenseApproval::getApplicationId, applicationId)
                        .orderByDesc(ExpenseApproval::getCreateTime)
                        .last("LIMIT 1")
        );
        if (approval != null) vo.setApproval(convertApprovalToVO(approval));

        if (application.getStatus() == 1) {
            ExpenseTransaction transaction = transactionService.lambdaQuery()
                    .eq(ExpenseTransaction::getApplicationId, applicationId)
                    .one();
            if (transaction != null) vo.setTransaction(convertTransactionToVO(transaction));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawApplication(Long applicationId) {
        ExpenseApplication application = this.getById(applicationId);
        if (application == null) {
            throw BusinessException.businessError("申请不存在");
        }
        
        SysUser currentUser = sysUserService.getCurrentUser();
        
        if (!currentUser.getId().equals(application.getApplicantId())) {
            throw BusinessException.businessError("只有申请人可以撤回申请");
        }
        
        if (application.getStatus() != 0) {
            throw BusinessException.businessError("只有待审批状态的申请可以撤回");
        }
        
        application.setStatus(3);
        application.setUpdateTime(LocalDateTime.now());
        this.updateById(application);
    }

    private ExpenseApplicationVO convertToVO(ExpenseApplication entity) {
        ExpenseApplicationVO vo = new ExpenseApplicationVO();
        BeanUtils.copyProperties(entity, vo);
        ClubInfo club = clubInfoService.getById(entity.getClubId());
        if (club != null) vo.setClubName(club.getClubName());
        SysUser applicant = sysUserService.getById(entity.getApplicantId());
        if (applicant != null) vo.setApplicantName(applicant.getName());
        if (entity.getActivityId() != null) {
            ActivityInfo activity = activityInfoService.getById(entity.getActivityId());
            if (activity != null) vo.setActivityName(activity.getActivityName());
        }
        vo.setStatusText(getStatusText(entity.getStatus()));
        return vo;
    }

    private ExpenseApprovalVO convertApprovalToVO(ExpenseApproval entity) {
        ExpenseApprovalVO vo = new ExpenseApprovalVO();
        BeanUtils.copyProperties(entity, vo);
        SysUser approver = sysUserService.getById(entity.getApproverId());
        if (approver != null) vo.setApproverName(approver.getName());
        return vo;
    }

    private ExpenseTransactionVO convertTransactionToVO(ExpenseTransaction entity) {
        ExpenseTransactionVO vo = new ExpenseTransactionVO();
        BeanUtils.copyProperties(entity, vo);
        ClubInfo club = clubInfoService.getById(entity.getClubId());
        if (club != null) vo.setClubName(club.getClubName());
        if (entity.getActivityId() != null) {
            ActivityInfo activity = activityInfoService.getById(entity.getActivityId());
            if (activity != null) vo.setActivityName(activity.getActivityName());
        }
        SysUser creator = sysUserService.getById(entity.getCreateBy());
        if (creator != null) vo.setCreateByName(creator.getName());
        vo.setTypeText(entity.getType() == 1 ? "收入" : "支出");
        return vo;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已驳回";
            case 3: return "已撤销";
            default: return "未知";
        }
    }
}