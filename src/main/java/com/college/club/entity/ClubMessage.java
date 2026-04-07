package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 社团消息实体（完全关联 sys_user 和 club_info）
 */
@Data
@TableName("club_message")
public class ClubMessage {
    /**
     * 消息ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收人ID，关联 sys_user.id
     */
    private Long receiverId;

    /**
     * 所属社团ID，关联 club_info.id
     */
    private Long clubId;

    /**
     * 社团名称（冗余存储，和 club_info.club_name 保持一致）
     */
    private String clubName;

    /**
     * 消息内容（管理员手动编写）
     */
    private String content;

    /**
     * 是否已读：0=未读，1=已读
     */
    private Integer isRead;

    /**
     * 发送时间
     */
    private Date createTime;
}