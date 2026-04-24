package com.college.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ClubInfoVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubAuditDTO;
import com.college.club.dto.CreateClubDTO;
import com.college.club.dto.JoinClubDTO;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.service.ClubInfoService;
import com.college.club.service.SysUserService;
import com.college.club.service.UserClubRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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

    @Resource
    private SysUserService sysUserService;

    /**
     * 创建社团接口
     * 路径：/api/club/create
     * 方法：POST
     */

    @Operation(summary = "创建社团")
    @PostMapping("/create")
    public Result<?> createClub(@Valid @RequestBody CreateClubDTO dto) {

        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        // 调用业务层创建社团的方法，返回结果给前端
        return clubInfoService.createClub(dto,userId);
    }

    /**
     * 加入社团接口
     * 路径：/api/club/join
     * 方法：POST
     */

    @Operation(summary = "加入社团")
    @PostMapping("/join")
    public Result<?> joinClub(@Valid @RequestBody JoinClubDTO dto) {
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        // 调用业务层加入社团的方法，返回结果给前端
        return userClubRelationService.joinClub(dto,userId);
    }


    /**
     * 社团审核接口（仅老师可操作）
     * 路径：/api/club/audit
     * 方法：POST
     */
    @Operation(summary = "社团审核")
    @PostMapping("/audit")
    public Result<String> auditClub(@Valid @RequestBody ClubAuditDTO dto) {

        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();

        // 调用业务层的审核方法，返回结果给前端
        return clubInfoService.auditClub(dto);
    }

    /**
     * 修改社团信息接口（仅负责人可操作）
     * 路径：/api/club/update
     * 方法：POST
     */
    @Operation(summary = "修改社团信息")
    @PostMapping("/update")
    public Result<?> updateClub(@Valid @RequestBody CreateClubDTO dto) {
        // 调用业务层修改社团信息的方法，返回结果给前端
        return clubInfoService.updateClub(dto);
    }

    //社团列表接口

    @Operation(summary = "社团列表")
    @GetMapping("list")
    public Result<PageVO<ClubInfoVO>> getClubList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize

    ) {

        return clubInfoService.getClubList(status,pageNum,pageSize);
    }

    //社团详情接口

    @Operation(summary = "社团详情")
    @GetMapping("/{id}")
    public Result<ClubInfoVO> getClubDetail(@PathVariable Long id) {
        return clubInfoService.getClubDetail(id);
    }


    //待审核社团列表
    @Operation(summary = "待审核社团列表")
    @GetMapping("/audit-list")
    public Result<PageVO<ClubInfoVO>> getAuditList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return clubInfoService.getAuditList(pageNum, pageSize);
    }

    @Operation(summary = "我的社团")
    @GetMapping("/my")
    public Result<PageVO<ClubInfoVO>> getMyClubs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return clubInfoService.getMyClubs(pageNum, pageSize);
    }

    @GetMapping("/my/manage")
    public Result<?> getMyManageClubs() {
        return clubInfoService.getMyManageClubs();
    }


    @Operation(summary = "我管理的社团", description = "查询当前用户作为负责人或指导老师的社团列表")
    @GetMapping("/my-managed")
    public Result<PageVO<ClubInfoVO>> getMyManagedClubs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return clubInfoService.getMyManagedClubs(pageNum, pageSize);
    }






}