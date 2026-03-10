package com.college.club.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_announcement") // 对应数据库表名
public class ClubAnnouncement {

    @TableId(type = IdType.AUTO) // 主键自增
    private Long id; // 公告唯一标识ID

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容（支持富文本/HTML）
     */
    private String content;

    /**
     * 发布人ID（关联sys_user.id）
     */
    private Long publisherId;

    /**
     * 所属社团ID（关联club_info.id）
     */
    private Long clubId;

    /**
     * 发布时间
     */
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime publishTime;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private LocalDateTime updateTime;

    /**
     * 公告状态：0-草稿 1-已发布 2-已下架
     */
    private Integer status;

    /**
     * 是否置顶：0-否 1-是
     */
    private Integer isTop;

    /**
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 扩展信息（JSON格式，如附件、可见范围）
     */
    private String extendInfo; // 若用JSON处理，也可定义为Map或自定义对象
}
