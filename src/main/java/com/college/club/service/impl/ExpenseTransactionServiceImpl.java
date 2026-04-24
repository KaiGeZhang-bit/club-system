package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ExpenseTransactionVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.TransactionPageVO;
import com.college.club.entity.*;
import com.college.club.mapper.ExpenseTransactionMapper;
import com.college.club.service.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseTransactionServiceImpl extends ServiceImpl<ExpenseTransactionMapper, ExpenseTransaction>
        implements ExpenseTransactionService {

    @Autowired
    private ClubInfoService clubInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ActivityInfoService activityInfoService;
    
    @Autowired
    private ClubBudgetService clubBudgetService;

    @Autowired
    private ExpenseTransactionMapper transactionMapper;

    /**
     * 分页查询交易流水列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param clubId 社团ID（可选）
     * @param type 交易类型：1=收入，2=支出（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果，包含基于查询条件的统计数据
     */
    @Override
    public TransactionPageVO queryTransactionList(Integer pageNum, Integer pageSize,
                                                  Long clubId, Integer type,
                                                  LocalDateTime startTime, LocalDateTime endTime) {
        // 获取当前登录用户
        SysUser currentUser = sysUserService.getCurrentUser();

        // 社团负责人只能查看自己管理的社团流水
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

        // 构建查询条件
        LambdaQueryWrapper<ExpenseTransaction> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(ExpenseTransaction::getClubId, clubId);
        if (type != null) wrapper.eq(ExpenseTransaction::getType, type);
        if (startTime != null) wrapper.ge(ExpenseTransaction::getTransactionTime, startTime);
        if (endTime != null) wrapper.le(ExpenseTransaction::getTransactionTime, endTime);
        wrapper.orderByDesc(ExpenseTransaction::getTransactionTime);

        // 【关键】先查询所有符合条件的数据用于统计（不受分页限制）
        // 如果没有查询条件，查询全部数据；如果有查询条件，查询符合条件的全部数据
        List<ExpenseTransaction> allFilteredTransactions = transactionMapper.selectList(wrapper);
        
        // 初始化统计变量
        BigDecimal totalIncome = BigDecimal.ZERO;   // 总收入
        BigDecimal totalExpense = BigDecimal.ZERO;  // 总支出
        
        // 遍历所有符合条件的交易记录，计算统计
        for (ExpenseTransaction transaction : allFilteredTransactions) {
            if (transaction.getType() == 1) {
                // 类型为1表示收入，累加到总收入
                totalIncome = totalIncome.add(transaction.getAmount());
            } else if (transaction.getType() == 2) {
                // 类型为2表示支出，累加到总支出
                totalExpense = totalExpense.add(transaction.getAmount());
            }
        }
        
        // 计算净收支 = 总收入 - 总支出
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        // 执行分页查询（只查询当前页的数据用于列表展示）
        Page<ExpenseTransaction> page = new Page<>(pageNum, pageSize);
        Page<ExpenseTransaction> resultPage = this.page(page, wrapper.clone());

        // 将当前页的实体列表转换为VO列表
        List<ExpenseTransactionVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 封装分页响应对象
        TransactionPageVO pageVO = new TransactionPageVO();
        pageVO.setRecords(voList);                      // 当前页数据列表
        pageVO.setTotal(resultPage.getTotal());         // 总记录数
        pageVO.setPages(resultPage.getPages());         // 总页数
        pageVO.setCurrent((int) resultPage.getCurrent()); // 当前页码
        pageVO.setSize((int) resultPage.getSize());     // 每页条数
        pageVO.setTotalIncome(totalIncome);             // 总收入（基于查询条件的全部数据）
        pageVO.setTotalExpense(totalExpense);           // 总支出（基于查询条件的全部数据）
        pageVO.setNetBalance(netBalance);               // 净收支（基于查询条件的全部数据）
        return pageVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIncome(Long clubId, BigDecimal amount, String source, String description, Long activityId) {
        SysUser currentUser = sysUserService.getCurrentUser();
        
        if (currentUser.getRole() != 1 && currentUser.getRole() != 2) {
            throw BusinessException.businessError("只有社团负责人和老师可以添加收入记录");
        }
        
        if (currentUser.getRole() == 1) {
            ClubInfo club = clubInfoService.getById(clubId);
            if (club == null || !currentUser.getId().equals(club.getLeaderId())) {
                throw BusinessException.businessError("您只能为自己负责的社团添加收入");
            }
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.paramError("收入金额必须大于0");
        }
        
        ExpenseTransaction transaction = new ExpenseTransaction();
        transaction.setClubId(clubId);
        transaction.setType(1);
        transaction.setAmount(amount);
        transaction.setSource(source != null ? source : "其他收入");
        transaction.setActivityId(activityId);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setDescription(description);
        transaction.setCreateBy(currentUser.getId());
        transaction.setCreateTime(LocalDateTime.now());
        
        this.save(transaction);
    }

    private ExpenseTransactionVO convertToVO(ExpenseTransaction entity) {
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
}