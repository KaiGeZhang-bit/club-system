package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.ExpenseTransactionVO;
import com.college.club.common.vo.PageVO;
import com.college.club.entity.ExpenseTransaction;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

/**
 * 经费流水服务接口
 */

public interface ExpenseTransactionService extends IService<ExpenseTransaction> {

    /**
     * 分页查询经费流水
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param clubId 社团ID（可选）
     * @param type 交易类型（1收入 2支出，可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    PageVO<ExpenseTransactionVO> queryTransactionList(Integer pageNum, Integer pageSize,
                                                      Long clubId, Integer type,
                                                      LocalDateTime startTime, LocalDateTime endTime);
}