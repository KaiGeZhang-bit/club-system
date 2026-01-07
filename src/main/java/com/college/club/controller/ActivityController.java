package com.college.club.controller;

import com.college.club.common.vo.Result;
import com.college.club.dto.ActivityDTO;
import com.college.club.service.ActivityInfoService;
import com.college.club.common.vo.ActivityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "活动管理", description = "活动创建、查询、状态更新、删除接口")
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Resource
    private ActivityInfoService activityInfoService;

    // 1. 创建活动（已显示）
    @Operation(summary = "创建活动")
    @PostMapping("/create")
    public Result<ActivityVO> createActivity(@Valid @RequestBody ActivityDTO dto) {
        return activityInfoService.createActivity(dto);
    }

    // 2. 查询活动列表（补充后会显示）
    @Operation(summary = "查询活动列表")
    @GetMapping("/list")
    public Result<List<ActivityVO>> getActivityList(
            @Parameter(description = "活动状态：0待审核/1已发布/2进行中/3已结束（可选）") Integer status,
            @Parameter(description = "社团ID（可选）") Long clubId) {
        return activityInfoService.getActivityList(status, clubId);
    }

    // 3. 查询活动详情（补充后会显示）
    @Operation(summary = "查询活动详情")
    @GetMapping("/{id}")
    public Result<ActivityVO> getActivityById(@Parameter(description = "活动ID（必填）") @PathVariable Long id) {
        return activityInfoService.getActivityById(id);
    }

    // 4. 更新活动状态（补充后会显示）
    @Operation(summary = "更新活动状态")
    @PutMapping("/status/{id}")
    public Result<?> updateActivityStatus(
            @Parameter(description = "活动ID（必填）") @PathVariable Long id,
            @Parameter(description = "目标状态：0待审核/1已发布/2进行中/3已结束（必填）") @RequestParam Integer status) {
        return activityInfoService.updateActivityStatus(id, status);
    }

    // 5. 删除活动（补充后会显示）
    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    public Result<?> deleteActivity(@Parameter(description = "活动ID（必填）") @PathVariable Long id) {
        return activityInfoService.deleteActivity(id);
    }
}