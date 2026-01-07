package com.college.club.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 我的退出申请展示VO
 */
@Data
public class MyQuitApplyVO {
    // 申请基础信息
    private Long id; // 申请ID
    private Long clubId; // 社团ID
    private String clubName; // 社团名称
    private String applyReason; // 申请理由
    private LocalDateTime applyTime; // 申请时间

    // 审核信息
    private Integer auditStatus; // 审核状态编码（0=待审核，1=通过，2=驳回）
    private String auditStatusDesc; // 审核状态文字描述
    private String auditRemark; // 驳回备注
    private LocalDateTime auditTime; // 审核时间
    private Long auditorId; // 审核人ID
}