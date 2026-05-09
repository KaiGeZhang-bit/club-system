package com.college.club.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员查看待审核退出申请VO
 */
@Data
public class PendingQuitApplyVO {
    // 申请基础信息
    private Long id; // 申请ID
    private Long userId; // 申请人ID
    private String userName; // 申请人姓名
    private Long clubId; // 社团ID
    private String clubName; // 社团名称
    private String applyReason; // 退出理由
    private LocalDateTime applyTime; // 申请时间

    // 审核状态
    private Integer status; // 状态编码（0=待审核）
    private String statusDesc; // 状态描述
}
