package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ExpenseStatisticsVO;
import com.college.club.entity.ClubBudget;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.ExpenseApplication;
import com.college.club.entity.ExpenseTransaction;
import com.college.club.entity.SysUser;
import com.college.club.mapper.ClubBudgetMapper;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.ExpenseApplicationMapper;
import com.college.club.mapper.ExpenseTransactionMapper;
import com.college.club.service.ExpenseStatisticsService;
import com.college.club.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseStatisticsServiceImpl implements ExpenseStatisticsService {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private ClubInfoMapper clubInfoMapper;
    
    @Autowired
    private ExpenseTransactionMapper transactionMapper;
    
    @Autowired
    private ExpenseApplicationMapper applicationMapper;
    
    @Autowired
    private ClubBudgetMapper budgetMapper;

    @Override
    public ExpenseStatisticsVO getClubStatistics(Long clubId) {
        SysUser currentUser = sysUserService.getCurrentUser();
        
        if (clubId == null) {
            if (currentUser.getRole() == 1) {
                ClubInfo club = clubInfoMapper.selectOne(
                    new LambdaQueryWrapper<ClubInfo>().eq(ClubInfo::getLeaderId, currentUser.getId())
                );
                if (club != null) {
                    clubId = club.getId();
                }
            }
        }
        if (clubId == null) {
            throw BusinessException.businessError("请指定社团ID");
        }
        checkPermission(currentUser, clubId);
        return calculateStatistics(clubId);
    }

    @Override
    public ExpenseStatisticsVO getMyManagedClubStatistics() {
        SysUser currentUser = sysUserService.getCurrentUser();
        
        if (currentUser.getRole() != 1) {
            throw BusinessException.businessError("只有社团负责人可以查询此数据");
        }
        ClubInfo club = clubInfoMapper.selectOne(
            new LambdaQueryWrapper<ClubInfo>().eq(ClubInfo::getLeaderId, currentUser.getId())
        );
        
        if (club == null) {
            throw BusinessException.businessError("您还不是任何社团的负责人");
        }
        return calculateStatistics(club.getId());
    }
    private ExpenseStatisticsVO calculateStatistics(Long clubId) {
        ExpenseStatisticsVO vo = new ExpenseStatisticsVO();
        vo.setClubId(clubId);
        
        ClubInfo club = clubInfoMapper.selectById(clubId);
        if (club != null) {
            vo.setClubName(club.getClubName());
        }
        
        List<ExpenseTransaction> allTransactions = transactionMapper.selectList(
            new LambdaQueryWrapper<ExpenseTransaction>().eq(ExpenseTransaction::getClubId, clubId)
        );
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        
        BigDecimal monthIncome = BigDecimal.ZERO;
        BigDecimal monthExpense = BigDecimal.ZERO;
        
        for (ExpenseTransaction transaction : allTransactions) {
            if (transaction.getType() == 1) {
                totalIncome = totalIncome.add(transaction.getAmount());
                if (transaction.getTransactionTime() != null && 
                    transaction.getTransactionTime().isAfter(monthStart)) {
                    monthIncome = monthIncome.add(transaction.getAmount());
                }
            } else if (transaction.getType() == 2) {
                totalExpense = totalExpense.add(transaction.getAmount());
                if (transaction.getTransactionTime() != null && 
                    transaction.getTransactionTime().isAfter(monthStart)) {
                    monthExpense = monthExpense.add(transaction.getAmount());
                }
            }
        }
        
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(totalIncome.subtract(totalExpense));
        vo.setMonthIncome(monthIncome);
        vo.setMonthExpense(monthExpense);
        
        ClubBudget budget = budgetMapper.selectOne(
            new LambdaQueryWrapper<ClubBudget>()
                .eq(ClubBudget::getClubId, clubId)
                .eq(ClubBudget::getYear, now.getYear())
                .isNull(ClubBudget::getQuarter)
        );
        
        if (budget != null) {
            vo.setBudgetTotal(budget.getTotalBudget());
            vo.setBudgetUsed(budget.getUsedBudget());
            if (budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
                double rate = budget.getUsedBudget()
                    .divide(budget.getTotalBudget(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
                vo.setBudgetUsageRate(rate);
            } else {
                vo.setBudgetUsageRate(0.0);
            }
        }
        
        Long applicationCount = applicationMapper.selectCount(
            new LambdaQueryWrapper<ExpenseApplication>().eq(ExpenseApplication::getClubId, clubId)
        );
        vo.setApplicationCount(applicationCount.intValue());
        
        Long pendingCount = applicationMapper.selectCount(
            new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getClubId, clubId)
                .eq(ExpenseApplication::getStatus, 0)
        );
        vo.setPendingApplicationCount(pendingCount.intValue());
        
        return vo;
    }

    private void checkPermission(SysUser user, Long clubId) {
        if (user.getRole() == 0) {
            ClubInfo club = clubInfoMapper.selectById(clubId);
            if (club == null) {
                throw BusinessException.businessError("社团不存在");
            }
        }
        
        if (user.getRole() == 1) {
            ClubInfo club = clubInfoMapper.selectById(clubId);
            if (club == null || !user.getId().equals(club.getLeaderId())) {
                throw BusinessException.businessError("您无权查看该社团的经费信息");
            }
        }
        
        if (user.getRole() == 2) {
            ClubInfo club = clubInfoMapper.selectById(clubId);
            if (club == null) {
                throw BusinessException.businessError("社团不存在");
            }
        }
    }
}
