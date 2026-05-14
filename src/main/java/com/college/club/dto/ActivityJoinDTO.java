package com.college.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityJoinDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    private Long userId;

    private String userName;

    @NotNull(message = "审核状态不能为空（1通过/2驳回）")
    private Integer auditStatus; // 审核状态：1通过/2驳回

    private Long auditorId; // 审核人ID（社团管理员ID）

    // 只有驳回时必填，后续Service层会校验
    private String auditRemark; // 审核备注


}
