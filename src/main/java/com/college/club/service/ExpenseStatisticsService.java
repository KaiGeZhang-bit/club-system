package com.college.club.service;

import com.college.club.common.vo.ExpenseStatisticsVO;

public interface ExpenseStatisticsService {
    
    ExpenseStatisticsVO getClubStatistics(Long clubId);
    
    ExpenseStatisticsVO getMyManagedClubStatistics();
}
