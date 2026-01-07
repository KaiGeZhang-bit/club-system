package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户-社团关联表实体（对应user_club_relation，无枚举）
 */
@Data
@TableName("user_club_relation")
public class UserClubRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("club_id")
    private Long clubId;

    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 关联状态：0=待审核，1=已加入，2=已退出
     */
    @TableField("status")
    private Integer status;

    @TableField("audit_time")
    private LocalDateTime auditTime;
}