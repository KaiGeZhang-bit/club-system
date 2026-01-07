package com.college.club.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动响应VO
 */
@Data
public class ActivityVO {
    private Long id;
    private String activityName;
    private Long clubId;
    private String clubName;
    private LocalDateTime activityTime;
    private String location;
    private Integer maxNum;
    private Integer joinNum;
    private String detail;
    private String poster;
    private Integer status;
    private String statusDesc; // 状态描述（如“待审核”）
    private LocalDateTime createTime;
}