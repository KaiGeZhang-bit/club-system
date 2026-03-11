package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.entity.ClubAnnouncement;
import com.college.club.entity.ClubInfo;
import com.college.club.mapper.ClubAnnouncementMapper;
import com.college.club.service.ClubAnnouncementService;
import com.college.club.service.ClubInfoService;
import com.college.club.common.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 社团公告Service实现层
 * 只处理业务逻辑，不写任何权限校验（权限由切面统一处理）
 */
@Service
public class ClubAnnouncementServiceImpl extends ServiceImpl<ClubAnnouncementMapper, ClubAnnouncement>
        implements ClubAnnouncementService {

    @Autowired
    private ClubInfoService clubInfoService;

    /**
     * 新增公告（纯业务逻辑，无权限校验）
     */
    @Override
    public Result<?> addAnnouncement(ClubAnnouncement announcement) {
        // 1. 业务校验：社团ID不能为空
        Long clubId = announcement.getClubId();
        if (clubId == null) {
            return Result.failBusiness("新增失败：公告必须关联社团（clubId不能为空）");
        }

        // 2. 业务校验：关联的社团存在
        ClubInfo clubInfo = clubInfoService.getById(clubId);
        if (clubInfo == null) {
            return Result.failBusiness("新增失败：关联的社团不存在");
        }

        // 3. 保存公告
        boolean saveSuccess = this.save(announcement);
        return saveSuccess ? Result.success("公告新增成功") : Result.failBusiness("公告新增失败");
    }

    /**
     * 删除公告（纯业务逻辑，无权限校验）
     */
    @Override
    public Result<?> deleteAnnouncement(Long id) {
        // 1. 业务校验：公告存在
        ClubAnnouncement announcement = this.getById(id);
        if (announcement == null) {
            return Result.failBusiness("删除失败：公告不存在");
        }

        // 2. 执行删除
        boolean deleteSuccess = this.removeById(id);
        return deleteSuccess ? Result.success("公告删除成功") : Result.failBusiness("公告删除失败");
    }

    /**
     * 修改公告（纯业务逻辑，无权限校验）
     */
    @Override
    public Result<?> updateAnnouncement(ClubAnnouncement announcement) {
        // 1. 业务校验：公告ID不能为空
        Long annId = announcement.getId();
        if (annId == null) {
            return Result.failBusiness("修改失败：公告ID不能为空");
        }

        // 2. 业务校验：公告存在
        if (!this.exists(new LambdaQueryWrapper<ClubAnnouncement>().eq(ClubAnnouncement::getId, annId))) {
            return Result.failBusiness("修改失败：公告不存在");
        }

        // 3. 业务校验：关联社团存在（如果传了clubId）
        Long clubId = announcement.getClubId();
        if (clubId != null && clubInfoService.getById(clubId) == null) {
            return Result.failBusiness("修改失败：关联的社团不存在");
        }

        // 4. 执行修改
        boolean updateSuccess = this.updateById(announcement);
        return updateSuccess ? Result.success("公告修改成功") : Result.failBusiness("公告修改失败");
    }

    /**
     * 查询公告（纯业务逻辑，无权限校验）
     */
    @Override
    public Result<?> getAnnouncementById(Long id) {
        ClubAnnouncement announcement = this.getById(id);
        if (announcement == null) {
            return Result.failBusiness("查询失败：公告不存在");
        }
        return Result.success(announcement);
    }
}