package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.dto.ApprovalDTO;
import com.college.club.entity.ExpenseApproval;
import org.springframework.stereotype.Service;

/**
 * 经费审批服务接口
 */

public interface ExpenseApprovalService extends IService<ExpenseApproval> {

    /**
     * 审批经费申请
     * @param applicationId 申请ID
     * @param dto 审批表单
     * @param approverId 审批人ID
     */
    void approveApplication(Long applicationId, ApprovalDTO dto, Long approverId);
}