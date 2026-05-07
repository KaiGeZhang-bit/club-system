package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.entity.ClubAnnouncement;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.mapper.ClubAnnouncementMapper;
import com.college.club.service.ClubAnnouncementService;
import com.college.club.service.ClubInfoService;
import com.college.club.common.vo.Result;
import com.college.club.service.SysUserService;
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
    @Autowired
    private SysUserService sysUserService;

    /**
     * 新增公告
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


    /**
     * 分页查询公告的实现方法
     */

    @Override
    public Result<?> getAnnouncementPage(Integer pageNum, Integer pageSize,Long clubId,Integer status,Integer isTop) {

        //1.设置分页默认值（防止前端不传参数导致报错
        if (pageNum == null || pageSize == null) {
            pageNum = 1;
            pageSize = 10;
        }

        //2.创建Mybatis-Plus的分页对象（核心：告诉插件要查第几页、每页几条
        Page<ClubAnnouncement> page = new Page<>(pageNum, pageSize);

        //3.构建查询条件（只筛选非null的参数，不传的条件自动忽略）
        LambdaQueryWrapper<ClubAnnouncement> wrapper = new LambdaQueryWrapper<>();
        //条件1.筛选指定社团的公告
        if (clubId != null) {
            wrapper.eq(ClubAnnouncement::getClubId, clubId);
        }
        //条件2.筛选指定状态的公告
        if (status != null) {
            wrapper.eq(ClubAnnouncement::getStatus, status);
        }
        //条件3.筛选是否置顶的公告
        if (isTop != null) {
            wrapper.eq(ClubAnnouncement::getIsTop, isTop);
        }

        //4.排序规则（先按置顶降序，再按发布时间降序，后续补充字段后生效
        wrapper.orderByDesc(ClubAnnouncement::getIsTop)
                .orderByDesc(ClubAnnouncement::getPublishTime);

        //5.执行分页查询
        Page<ClubAnnouncement> ResulttPage = this.page(page, wrapper);

        //6.返回结果
        return Result.success(ResulttPage);

    }

}