package com.college.club.controller;

import com.college.club.annotation.ClubManagerAuth;
import com.college.club.entity.ClubAnnouncement;
import com.college.club.service.ClubAnnouncementService;
import com.college.club.common.vo.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 社团公告控制器
 * 注解@ClubManagerAuth标记需要权限校验的接口，切面自动拦截
 */
@RestController
@RequestMapping("/api/announcement")
@Tag(name = "社团公告管理")
public class ClubAnnouncementController {

    @Autowired
    private ClubAnnouncementService announcementService;

    /**
     * 新增社团公告（需要权限校验）
     */
    @PostMapping("/add")
    @Operation(summary = "新增社团公告")
    @ClubManagerAuth // ✅ 加注解，触发切面权限校验
    public Result<?> addAnnouncement(@RequestBody ClubAnnouncement announcement) {
        return announcementService.addAnnouncement(announcement);
    }

    /**
     * 修改社团公告（需要权限校验）
     */
    @PutMapping("/update")
    @Operation(summary = "修改社团公告")
    @ClubManagerAuth // ✅ 加注解，触发切面权限校验
    public Result<?> updateAnnouncement(@RequestBody ClubAnnouncement announcement) {
        return announcementService.updateAnnouncement(announcement);
    }

    /**
     * 删除社团公告（需要权限校验）
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除社团公告")
    @ClubManagerAuth // ✅ 加注解，触发切面权限校验
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }

    /**
     * 根据ID查询公告（无需权限校验）
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "查询公告详情")
    // ❌ 不加注解，不触发权限校验
    public Result<?> getAnnouncementById(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }
}