package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.vo.ExpenseTransactionVO;
import com.college.club.common.vo.PageVO;
import com.college.club.entity.*;
import com.college.club.mapper.ExpenseTransactionMapper;
import com.college.club.service.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public PageVO<ExpenseTransactionVO> queryTransactionList(Integer pageNum, Integer pageSize,
                                                             Long clubId, Integer type,
                                                             LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ExpenseTransaction> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) wrapper.eq(ExpenseTransaction::getClubId, clubId);
        if (type != null) wrapper.eq(ExpenseTransaction::getType, type);
        if (startTime != null) wrapper.ge(ExpenseTransaction::getTransactionTime, startTime);
        if (endTime != null) wrapper.le(ExpenseTransaction::getTransactionTime, endTime);
        wrapper.orderByDesc(ExpenseTransaction::getTransactionTime);

        Page<ExpenseTransaction> page = new Page<>(pageNum, pageSize);
        Page<ExpenseTransaction> resultPage = this.page(page, wrapper);

        List<ExpenseTransactionVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageVO<ExpenseTransactionVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPages(resultPage.getPages());
        pageVO.setCurrent((int) resultPage.getCurrent());
        pageVO.setSize((int) resultPage.getSize());
        return pageVO;
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