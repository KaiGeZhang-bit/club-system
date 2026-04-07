package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.ExpenseApplicationVO;
import com.college.club.common.vo.PageVO;
import com.college.club.dto.ExpenseApplicationDTO;
import com.college.club.entity.ExpenseApplication;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

/**
 * 经费申请服务接口
 */


public interface ExpenseApplicationService extends IService<ExpenseApplication> {

    /**
     * 提交经费申请
     * @param dto 申请表单
     * @param applicantId 申请人ID（从当前登录用户获取）
     * @return 申请ID
     */
    Long submitApplication(ExpenseApplicationDTO dto, Long applicantId);

    /**
     * 分页查询申请列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param clubId 社团ID（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    PageVO<ExpenseApplicationVO> queryApplicationList(Integer pageNum, Integer pageSize,
                                                      Long clubId, Integer status,
                                                      LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取申请详情（包含审批记录、流水等）
     * @param applicationId 申请ID
     * @return 详情VO
     */
    ExpenseApplicationVO getApplicationDetail(Long applicationId);
}