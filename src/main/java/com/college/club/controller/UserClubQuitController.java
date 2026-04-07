package com.college.club.controller;


import com.college.club.common.vo.Result;
import com.college.club.dto.ClubQuitAuditDTO;
import com.college.club.dto.QuitClubDTO;
import com.college.club.entity.SysUser;
import com.college.club.service.SysUserService;
import com.college.club.service.UserClubQuitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quit")
@Tag(name = "退出社团相关", description = "退出社团，退出社团审核接口")
public class UserClubQuitController {

    @Resource
    private UserClubQuitService userClubQuitService;

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/submit")
    @Operation(summary = "退出社团")
    public Result<?> submitQuitClub(@Validated @RequestBody QuitClubDTO quitClubDTO) {
        SysUser  currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        // 直接调用Service方法，无需处理用户ID（Service内部自动获取）
        return userClubQuitService.QuitClub(quitClubDTO);
    }


    @PostMapping("/auditor")
    @Operation(summary = "退出社团审核")
    public Result<?> auditorQuitClub(@Validated @RequestBody ClubQuitAuditDTO clubQuitAuditDTO) {
        return userClubQuitService.ClubQuitAudit(clubQuitAuditDTO);
    }


    @GetMapping("/my-list")
    @Operation(summary = "我的退出申请")
    public Result<?> getMyQuitApplyList(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return userClubQuitService.getMyQuitApplyList(pageNum, pageSize);
    }

    @PostMapping
    @Operation(summary = "撤销我的退出申请")
    public Result<?> withdrawQuitClub(@RequestParam Long QuitApply) {
        return userClubQuitService.withdrawQuitApply(QuitApply);
    }

}
