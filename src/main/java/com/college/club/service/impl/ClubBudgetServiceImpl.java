package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.BudgetVO;
import com.college.club.dto.BudgetDTO;
import com.college.club.entity.ClubBudget;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.ExpenseApplication;
import com.college.club.entity.ExpenseTransaction;
import com.college.club.entity.SysUser;
import com.college.club.mapper.ClubBudgetMapper;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.ExpenseApplicationMapper;
import com.college.club.mapper.ExpenseTransactionMapper;
import com.college.club.service.ClubBudgetService;
import com.college.club.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubBudgetServiceImpl extends ServiceImpl<ClubBudgetMapper, ClubBudget> implements ClubBudgetService {

    @Autowired
    private ClubBudgetMapper clubBudgetMapper;

    @Autowired
    private ClubInfoMapper clubInfoMapper;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ExpenseApplicationMapper expenseApplicationMapper;
    
    @Autowired
    private ExpenseTransactionMapper expenseTransactionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBudget(BudgetDTO dto) {
        SysUser currentUser = sysUserService.getCurrentUser();

        if (currentUser.getRole() != 1 && currentUser.getRole() != 2) {
            throw BusinessException.businessError("只有社团负责人和老师可以设置预算");
        }

        if (currentUser.getRole() == 1) {
            ClubInfo club = clubInfoMapper.selectById(dto.getClubId());
            if (club == null || !currentUser.getId().equals(club.getLeaderId())) {
                throw BusinessException.businessError("您只能设置自己负责的社团预算");
            }
        }

        LambdaQueryWrapper<ClubBudget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubBudget::getClubId, dto.getClubId())
                .eq(ClubBudget::getYear, dto.getYear());

        if (dto.getQuarter() != null) {
            wrapper.eq(ClubBudget::getQuarter, dto.getQuarter());
        } else {
            wrapper.isNull(ClubBudget::getQuarter);
        }

        ClubBudget existingBudget = clubBudgetMapper.selectOne(wrapper);

        if (existingBudget != null) {
            existingBudget.setTotalBudget(dto.getTotalBudget());
            existingBudget.setUpdateTime(LocalDateTime.now());
            clubBudgetMapper.updateById(existingBudget);
        } else {
            ClubBudget budget = new ClubBudget();
            BeanUtils.copyProperties(dto, budget);
            budget.setUsedBudget(BigDecimal.ZERO);
            budget.setCreateTime(LocalDateTime.now());
            budget.setUpdateTime(LocalDateTime.now());
            clubBudgetMapper.insert(budget);
        }
    }

    @Override
    public BudgetVO getBudget(Long clubId, Integer year, Integer quarter) {
        SysUser currentUser = sysUserService.getCurrentUser();

        checkPermission(currentUser, clubId);

        LambdaQueryWrapper<ClubBudget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubBudget::getClubId, clubId)
                .eq(ClubBudget::getYear, year);

        if (quarter != null) {
            wrapper.eq(ClubBudget::getQuarter, quarter);
        } else {
            wrapper.isNull(ClubBudget::getQuarter);
        }

        ClubBudget budget = clubBudgetMapper.selectOne(wrapper);
        if (budget == null) {
            return null;
        }

        return convertToVO(budget);
    }

    @Override
    public List<BudgetVO> getBudgetList(Long clubId, Integer year) {
        SysUser currentUser = sysUserService.getCurrentUser();

        if (clubId != null) {
            checkPermission(currentUser, clubId);
        } else {
            if (currentUser.getRole() == 1) {
                ClubInfo club = clubInfoMapper.selectOne(
                        new LambdaQueryWrapper<ClubInfo>().eq(ClubInfo::getLeaderId, currentUser.getId())
                );
                if (club != null) {
                    clubId = club.getId();
                }
            }
        }

        LambdaQueryWrapper<ClubBudget> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) {
            wrapper.eq(ClubBudget::getClubId, clubId);
        }
        if (year != null) {
            wrapper.eq(ClubBudget::getYear, year);
        }
        wrapper.orderByDesc(ClubBudget::getYear)
                .orderByAsc(ClubBudget::getQuarter);

        List<ClubBudget> budgets = clubBudgetMapper.selectList(wrapper);
        return budgets.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUsedBudget(Long clubId, BigDecimal amount) {
        // 1. 查询该社团的年度预算（使用当前系统年份）
        LambdaQueryWrapper<ClubBudget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubBudget::getClubId, clubId)
                .eq(ClubBudget::getYear, LocalDateTime.now().getYear())
                .isNull(ClubBudget::getQuarter);

        ClubBudget budget = clubBudgetMapper.selectOne(wrapper);

        // 2. 如果找到了预算记录，直接累加金额
        if (budget != null) {
            budget.setUsedBudget(budget.getUsedBudget().add(amount));
            budget.setUpdateTime(LocalDateTime.now());
            clubBudgetMapper.updateById(budget);
        }
    }

    /**
     * 实时计算该社团今年的已使用预算
     * 通过查询ExpenseTransaction表中该社团今年的所有支出记录（type=2）来计算
     * 
     * @param clubId 社团ID
     * @param year 年份
     * @return 今年已使用的预算总额
     */
    private BigDecimal calculateUsedBudget(Long clubId, Integer year) {
        // 查询该社团今年的所有支出交易记录（type=2表示支出）
        LambdaQueryWrapper<ExpenseTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseTransaction::getClubId, clubId)
               .eq(ExpenseTransaction::getType, 2)  // 只统计支出
               .ge(ExpenseTransaction::getTransactionTime, LocalDateTime.of(year, 1, 1, 0, 0, 0))  // 今年1月1日开始
               .lt(ExpenseTransaction::getTransactionTime, LocalDateTime.of(year + 1, 1, 1, 0, 0, 0)); // 明年1月1日之前
        
        List<ExpenseTransaction> expenseTransactions = expenseTransactionMapper.selectList(wrapper);
        
        // 累加所有支出金额
        BigDecimal totalUsed = BigDecimal.ZERO;
        for (ExpenseTransaction transaction : expenseTransactions) {
            totalUsed = totalUsed.add(transaction.getAmount());
        }
        
        return totalUsed;
    }

    private void checkPermission(SysUser user, Long clubId) {
        if (user.getRole() == 0) {
            throw BusinessException.businessError("学生无权查看预算信息");
        }

        if (user.getRole() == 1) {
            ClubInfo club = clubInfoMapper.selectById(clubId);
            if (club == null || !user.getId().equals(club.getLeaderId())) {
                throw BusinessException.businessError("您无权查看该社团的预算信息");
            }
        }
    }

    private BudgetVO convertToVO(ClubBudget budget) {
        BudgetVO vo = new BudgetVO();
        BeanUtils.copyProperties(budget, vo);

        ClubInfo club = clubInfoMapper.selectById(budget.getClubId());
        if (club != null) {
            vo.setClubName(club.getClubName());
        }

        BigDecimal total = budget.getTotalBudget();
        
        // 【关键修改】实时计算已使用预算，而不是使用数据库中可能不准确的usedBudget字段
        // 查询该社团今年的所有支出记录来实时计算
        BigDecimal used = calculateUsedBudget(budget.getClubId(), budget.getYear());
        
        // 更新VO中的已使用预算为实时计算的结果
        vo.setUsedBudget(used);
        vo.setRemainingBudget(total.subtract(used));

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            double rate = used.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            vo.setUsageRate(rate);
        } else {
            vo.setUsageRate(0.0);
        }

        return vo;
    }
}
