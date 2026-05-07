package com.college.club.common.vo;

import lombok.Data;

import java.util.Date;

/**
 * 消息VO - 用于返回消息列表
 */
@Data
public class MessageVO {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者姓名
     */
    private String senderName;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者姓名
     */
    private String receiverName;

    /**
     * 社团ID
     */
    private Long clubId;

    /**
     * 社团名称
     */
    private String clubName;

    /**
     * 消息内容
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
