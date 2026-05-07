package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.BudgetVO;
import com.college.club.dto.BudgetDTO;
import com.college.club.entity.ClubBudget;

import java.math.BigDecimal;
import java.util.List;

public interface ClubBudgetService extends IService<ClubBudget> {
    
    void setBudget(BudgetDTO dto);
    
    BudgetVO getBudget(Long clubId, Integer year, Integer quarter);
    
    List<BudgetVO> getBudgetList(Long clubId, Integer year);

    void updateUsedBudget(Long clubId, BigDecimal amount);

}
