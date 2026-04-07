package com.college.club.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.dto.ApprovalDTO;
import com.college.club.entity.*;
import com.college.club.mapper.ExpenseApprovalMapper;
import com.college.club.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExpenseApprovalServiceImpl extends ServiceImpl<ExpenseApprovalMapper, ExpenseApproval>
        implements ExpenseApprovalService {


    @Autowired
    private ExpenseApplicationService applicationService;
    @Autowired
    private ClubInfoService clubInfoService;

    @Autowired
    private ExpenseTransactionService transactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(Long applicationId, ApprovalDTO dto, Long approverId) {
        ExpenseApplication application = applicationService.getById(applicationId);
        if (application == null) throw BusinessException.businessError("申请不存在");
        if (application.getStatus() != 0) throw BusinessException.businessError("该申请已被处理，无法重复审批");

        ClubInfo club = clubInfoService.getById(application.getClubId());
        if (club == null) throw BusinessException.businessError("社团信息异常");

        Long teacherId = club.getTeacherId();
        if (teacherId == null || !teacherId.equals(approverId))
            throw BusinessException.businessError("只有指导老师才能审批经费申请");

        ExpenseApproval approval = new ExpenseApproval();
        approval.setApplicationId(applicationId);
        approval.setApproverId(approverId);
        approval.setAction(dto.getAction());
        approval.setRemark(dto.getRemark());
        approval.setCreateTime(LocalDateTime.now());
        this.save(approval);

        Integer newStatus = dto.getAction() == 1 ? 1 : 2;
        application.setStatus(newStatus);
        application.setUpdateTime(LocalDateTime.now());
        applicationService.updateById(application);

        if (dto.getAction() == 1) {
            ExpenseTransaction transaction = new ExpenseTransaction();
            transaction.setClubId(application.getClubId());
            transaction.setType(2);
            transaction.setAmount(application.getAmount());
            transaction.setSource("活动经费支出");
            transaction.setApplicationId(applicationId);
            transaction.setActivityId(application.getActivityId());
            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setDescription(application.getPurpose());
            transaction.setCreateBy(approverId);
            transaction.setCreateTime(LocalDateTime.now());
            transactionService.save(transaction);
        }
    }
}