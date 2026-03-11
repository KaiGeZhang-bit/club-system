package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ClubAnnouncement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团公告Mapper接口
 *
 * @author 自定义作者
 * @since 2026-03-11
 */
@Mapper
public interface ClubAnnouncementMapper extends BaseMapper<ClubAnnouncement> {

    // MyBatis-Plus的BaseMapper已内置：
    // insert (新增)、deleteById (按ID删除)、updateById (按ID更新)、selectById (按ID查询)、selectList (列表查询)
    // 如需自定义条件查询，可在此添加（如按社团ID查询公告）
}