package com.college.club.common.vo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityJoinListVO {
    private Long userId;  //用户ID
    private String userName;  //用户姓名
    private LocalDateTime joinTime;  //报名时间
    private String signStatusDesc;  //签到状态 未签到(0),已签到(1)

    private Integer auditStatus; // 审核状态码
    private String auditStatusName; // 状态名称（待审核/通过/驳回）
    private LocalDateTime auditTime; // 审核时间
    private String auditRemark; // 审核备注
    private Long auditorId; // 审核人ID

}
