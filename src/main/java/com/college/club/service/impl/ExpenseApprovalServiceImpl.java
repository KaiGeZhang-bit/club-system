package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.dto.ApprovalDTO;
import com.college.club.entity.*;
import com.college.club.mapper.ClubBudgetMapper;
import com.college.club.mapper.ExpenseApprovalMapper;
import com.college.club.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Autowired
    private ClubBudgetMapper clubBudgetMapper;

    @Autowired
    private ClubBudgetService clubBudgetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(Long applicationId, ApprovalDTO dto, Long approverId) {
        ExpenseApplication application = applicationService.getById(applicationId);
        if (application == null) throw BusinessException.businessError("申请不存在");
        if (application.getStatus() != 0) throw BusinessException.businessError("该申请已被处理，无法重复审批");

        ClubInfo club = clubInfoService.getById(application.getClubId());
        if (club == null) throw BusinessException.businessError("社团信息异常");

        SysUser approver = null;
        if (club.getTeacherId() != null && club.getTeacherId().equals(approverId)) {
            approver = new SysUser();
            approver.setId(approverId);
            approver.setRole(2);
        }

        if (approver == null) {
            throw BusinessException.businessError("只有指导老师才能审批经费申请");
        }

        if (dto.getAction() == 1) {
            ClubBudget budget = clubBudgetMapper.selectOne(
                    new LambdaQueryWrapper<ClubBudget>()
                            .eq(ClubBudget::getClubId, application.getClubId())
                            .eq(ClubBudget::getYear, LocalDateTime.now().getYear())
                            .isNull(ClubBudget::getQuarter)
            );

            if (budget != null) {
                BigDecimal remainingBudget = budget.getTotalBudget().subtract(budget.getUsedBudget());
                if (application.getAmount().compareTo(remainingBudget) > 0) {
                    throw BusinessException.businessError(
                            String.format("申请金额%.2f元超过剩余预算%.2f元，无法通过",
                                    application.getAmount().doubleValue(),
                                    remainingBudget.doubleValue())
                    );
                }

                double usageRate = budget.getUsedBudget()
                        .add(application.getAmount())
                        .divide(budget.getTotalBudget(), 4, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();

                if (usageRate > 0.9) {
                    System.out.println("警告：预算使用率已超过90%");
                }
            }
        }

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

            clubBudgetService.updateUsedBudget(application.getClubId(), application.getAmount());
        }
    }
}