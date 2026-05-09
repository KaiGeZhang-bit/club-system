package com.college.club.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员查看待审核加入申请VO
 */
@Data
public class PendingJoinApplyVO {
    // 申请基础信息
    private Long id; // 申请ID
    private Long userId; // 申请人ID
    private String userName; // 申请人姓名
    private Long clubId; // 社团ID
    private String clubName; // 社团名称
    private LocalDateTime joinTime; // 申请时间

    // 审核状态
    private Integer status; // 状态编码（0=待审核）
    private String statusDesc; // 状态描述
}
