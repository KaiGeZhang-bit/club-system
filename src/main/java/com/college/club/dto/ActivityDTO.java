package com.college.club.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 创建活动的请求参数DTO
 */
@Data
public class ActivityDTO {
    @NotBlank(message = "活动名称不能为空")
    private String activityName; // 活动名称

    @NotNull(message = "所属社团ID不能为空")
    private Long clubId; // 社团ID

    @NotBlank(message = "活动地点不能为空")
    private String location; // 活动地点

    @NotNull(message = "活动时间不能为空")
    @Future(message = "活动时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-M-d'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityTime; // 活动时间

    @NotNull(message = "最大参与人数不能为空")
    private Integer maxNum; // 最大参与人数（对应max_num）

    private String detail; // 活动详情（可选）
    private String poster; // 海报URL（可选）
}