package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体类，与activity_info表完全映射
 */
@Data
@TableName("activity_info") // 对应数据库表名
public class ActivityInfo {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    private String activityName; // 对应activity_name（必填）
    private Long clubId;         // 对应club_id（必填）
    private String clubName;     // 对应club_name（必填）

    @JsonFormat(pattern = "yyyy-M-d'T'HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime activityTime; // 对应activity_time（必填）
    private String location;     // 对应location（必填）
    private Integer maxNum;      // 对应max_num（最大参与人数，必填）
    private Integer joinNum;     // 对应join_num（已报名人数，必填）
    private String detail;       // 对应detail（活动详情，可选）
    private String poster;       // 对应poster（海报URL，可选）
    private Integer status;      // 对应status（状态：0待审核/1已发布等，必填）
    private LocalDateTime createTime; // 对应create_time（创建时间，必填）
    private LocalDateTime updateTime; // 对应update_time（更新时间，必填）
}