package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.entity.ClubAnnouncement;
import com.college.club.common.vo.Result;

/**
 * 社团公告Service接口
 * 包含公告的新增、删除、修改、查询等核心方法
 */
public interface ClubAnnouncementService extends IService<ClubAnnouncement> {

    /**
     * 新增社团公告（适配AOP权限校验，需传入关联的社团ID）
     * @param announcement 公告对象（包含clubId，关联社团表的id）
     * @return 操作结果
     */
    Result<?> addAnnouncement(ClubAnnouncement announcement);

    /**
     * 删除社团公告
     * @param id 公告ID
     * @return 操作结果
     */
    Result<?> deleteAnnouncement(Long id);

    /**
     * 修改社团公告
     * @param announcement 公告对象（包含id和clubId）
     * @return 操作结果
     */
    Result<?> updateAnnouncement(ClubAnnouncement announcement);

    /**
     * 根据ID查询公告
     * @param id 公告ID
     * @return 公告详情
     */
    Result<?> getAnnouncementById(Long id);
}