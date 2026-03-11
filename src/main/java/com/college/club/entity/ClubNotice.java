package com.college.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社团用户通知实体类
 *
 * @author 自定义作者名
 * @since 2026-03-11
 */
@Data
@TableName("club_notice")
public class ClubNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID（关联sys_user.id）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 所属社团ID（关联club_info.id）
     */
    @TableField("club_id")
    private Long clubId;

    /**
     * 关联公告ID（关联club_announcement.id，系统通知可为null）
     */
    @TableField("announcement_id")
    private Long announcementId;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 阅读状态：0-未读，1-已读
     */
    @TableField("read_status")
    private Integer readStatus;

    /**
     * 通知类型：0-公告通知，1-活动通知，2-系统通知
     */
    @TableField("notice_type")
    private Integer noticeType;

    /**
     * 发送渠道：0-站内信，1-短信，2-邮件，3-小程序推送
     */
    @TableField("send_channel")
    private Integer sendChannel;

    /**
     * 发送状态：0-待发送，1-发送成功，2-发送失败，3-已重试
     */
    @TableField("send_status")
    private Integer sendStatus;

    /**
     * 发送失败原因
     */
    @TableField("fail_reason")
    private String failReason;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private Date readTime;

    /**
     * 过期时间（自动失效）
     */
    @TableField("expire_time")
    private Date expireTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    // 可选：添加状态枚举常量（提升代码可读性）
    public static final Integer READ_STATUS_UNREAD = 0;
    public static final Integer READ_STATUS_READ = 1;

    public static final Integer NOTICE_TYPE_ANNOUNCEMENT = 0;
    public static final Integer NOTICE_TYPE_ACTIVITY = 1;
    public static final Integer NOTICE_TYPE_SYSTEM = 2;

    public static final Integer SEND_CHANNEL_INNER = 0;
    public static final Integer SEND_CHANNEL_SMS = 1;
    public static final Integer SEND_CHANNEL_EMAIL = 2;
    public static final Integer SEND_CHANNEL_MINI_APP = 3;

    public static final Integer SEND_STATUS_PENDING = 0;
    public static final Integer SEND_STATUS_SUCCESS = 1;
    public static final Integer SEND_STATUS_FAIL = 2;
    public static final Integer SEND_STATUS_RETRY = 3;
}