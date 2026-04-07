package com.college.club.controller;

import com.college.club.common.vo.ActivityJoinListVO;
import com.college.club.common.vo.ActivitySignQrVo;
import com.college.club.common.vo.Result;
import com.college.club.dto.ActivityJoinDTO;
import com.college.club.dto.ScanSignReqDTO;
import com.college.club.entity.ActivityJoin;
import com.college.club.entity.SysUser;
import com.college.club.service.ActivityJoinService;

import com.college.club.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;


import java.util.List;

@Tag(name = "活动相关管理", description = "报名、取消报名、审核报名、活动签到等接口")
@RestController // 告诉代码：这是接收前端请求的类
@RequestMapping("/api/activity/join") // 接口路径前缀
public class ActivityJoinController {

    @Resource
    private ActivityJoinService activityJoinService;
    @Resource
    private SysUserService sysUserService;

    // 报名活动接口
    @Operation(summary = "报名活动")
    @PostMapping("/apply")
    public Result<?> joinActivity(
            @RequestParam Long activityId// 活动ID

    ) {
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        return activityJoinService.joinActivity(activityId, userId);

    }

    @Operation(summary = "取消报名")
    @PostMapping("/cancel")
    public Result<?> cancelJoin(@RequestParam Long activityId) {
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();
        return activityJoinService.cancelJoin(activityId, userId);
    }

    //查询活动报名名单
    @Operation(summary = "查询指定活动的报名名单")
    @GetMapping("/list/{activityId}") // 重点：路径是/list/{activityId}（包含{}完整包裹）
    public Result<List<ActivityJoinListVO>> getJoinList(
            @PathVariable Long activityId
    ) {
        return activityJoinService.getJoinListByActivityId(activityId);
    }

    // 4. 审核活动报名（新增接口，适配Result）
    @Operation(summary = "审核活动报名")
    @PostMapping("/audit")
    public Result<?> auditActivityJoin(@Valid @RequestBody ActivityJoinDTO dto) {
        try {
            return activityJoinService.auditActivityJoin(dto);
        } catch (Exception e) {
            return Result.failSystem("审核报名失败：" + e.getMessage());
        }
    }

    // 5. 查询单个用户的报名审核状态（新增接口，适配Result）
    @Operation(summary = "查询单个用户的报名审核状态")
    @GetMapping("/audit/status")
    public Result<ActivityJoinListVO> getAuditStatus(
            @Parameter(description = "活动ID（必填）") @RequestParam Long activityId) {
        try {
            // 获取当前登录用户
            SysUser currentUser = sysUserService.getCurrentUser();
            if (currentUser == null) {
                return (Result<ActivityJoinListVO>) Result.failBusiness("请先登录");
            }
            Long userId = currentUser.getId();

            // 参数校验
            if (activityId == null || activityId <= 0) {
                return (Result<ActivityJoinListVO>) Result.failParam("活动ID不能为空且必须为正数");
            }
            // userId 由后端获取，无需校验

            return activityJoinService.getAuditStatus(activityId, userId);
        } catch (Exception e) {
            return (Result<ActivityJoinListVO>) Result.failSystem("查询审核状态失败：" + e.getMessage());
        }
    }


    @GetMapping("generateQr/{activityId}")
    @Operation(summary = "生成签到二维码")
    public Result<ActivitySignQrVo> generateSignQR(@PathVariable Long activityId) {
        return activityJoinService.generateSignQr(activityId);
    }


    @PostMapping("/scan")
    @Operation(summary = "用户扫码签到")
    public Result<?> scanActivity(@Valid @RequestBody ScanSignReqDTO reqDTO, HttpServletRequest request) {
        // 获取当前登录用户
        SysUser currentUser = sysUserService.getCurrentUser();
        if (currentUser == null) {
            return Result.failBusiness("请先登录");
        }
        Long userId = currentUser.getId();

        // 设置 IP（若前端未传则自动获取）
        if (!StringUtils.hasText(reqDTO.getSignIp())) {
            reqDTO.setSignIp(getClientRealIp(request));
        }

        // 调用 Service，将 userId 传入
        return activityJoinService.scanSign(reqDTO, userId);
    }
    /**
     * 工具方法：获取客户端真实IP（适配Nginx代理/本地测试/多代理场景）
     * @param request HTTP请求上下文
     * @return 客户端真实IP
     */
    private String getClientRealIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        // 依次读取不同代理头，兼容各类部署场景
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        // 兜底：无代理时取远程地址
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多代理场景：取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }




}