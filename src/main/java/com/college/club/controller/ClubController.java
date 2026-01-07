package com.college.club.controller;

import com.college.club.common.vo.Result;
import com.college.club.dto.ClubAuditDTO;
import com.college.club.dto.CreateClubDTO;
import com.college.club.dto.JoinClubDTO;
import com.college.club.service.ClubInfoService;
import com.college.club.service.UserClubRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 社团管理控制器（处理社团创建、加入等请求）
 */

@Tag(name = "社团管理", description = "社团创建、社团加入、社团审核接口")
@RestController // 标记为REST接口控制器
@RequestMapping("/api/club") // 所有社团接口的基础路径
public class ClubController {

    // 注入社团创建相关的业务层（处理创建社团逻辑）
    @Resource
    private ClubInfoService clubInfoService;

    // 注入社团加入相关的业务层（处理加入社团逻辑）
    @Resource
    private UserClubRelationService userClubRelationService;

    /**
     * 创建社团接口
     * 路径：/api/club/create
     * 方法：POST
     */

    @Operation(summary = "创建社团")
    @PostMapping("/create")
    public Result<?> createClub(@Valid @RequestBody CreateClubDTO dto) {
        // 调用业务层创建社团的方法，返回结果给前端
        return clubInfoService.createClub(dto);
    }

    /**
     * 加入社团接口
     * 路径：/api/club/join
     * 方法：POST
     */

    @Operation(summary = "加入社团")
    @PostMapping("/join")
    public Result<?> joinClub(@Valid @RequestBody JoinClubDTO dto) {
        // 调用业务层加入社团的方法，返回结果给前端
        return userClubRelationService.joinClub(dto);
    }


    /**
     * 社团审核接口（仅老师可操作）
     * 路径：/api/club/audit
     * 方法：POST
     */
    @Operation(summary = "社团审核")
    @PostMapping("/audit")
    public Result<String> auditClub(@Valid @RequestBody ClubAuditDTO dto) {
        // 调用业务层的审核方法，返回结果给前端
        return clubInfoService.auditClub(dto);
    }
}