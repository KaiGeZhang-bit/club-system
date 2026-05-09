package com.college.club.controller;


import com.college.club.common.vo.Result;
import com.college.club.dto.ClubJoinAuditDTO;
import com.college.club.entity.UserClubRelation;
import com.college.club.service.UserClubRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@Tag(name = "用户加入社团审核", description = "用户加入社团审核接口")
@RestController
@RequestMapping("/api/club/join")
public class UserClubRelationController {
    @Resource
    private UserClubRelationService userClubRelationService;
    @Operation(summary = "加入社团申请")
    @PostMapping("/audit")
    public Result<String> auditJoinApply(@Valid @RequestBody ClubJoinAuditDTO dto){
        return userClubRelationService.auditJoinApply(dto);
    }

    @GetMapping("/my-list")
    @Operation(summary = "我的加入社团申请")
    public Result<?> getMyQuitApplyList(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return userClubRelationService.MyJoinApply(pageNum, pageSize);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "撤销申请")
    public Result<?> withdrawApply(@RequestParam(value = "relationId", required = false) Long relationId){
        return  userClubRelationService.withdraw(relationId);
    }

    @GetMapping("/pending-list")
    @Operation(summary = "待审核加入申请列表（管理员/老师）")
    public Result<?> getPendingJoinApplyList(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long clubId
    ) {
        return userClubRelationService.getPendingJoinApplyList(pageNum, pageSize, clubId);
    }
}