package com.college.club.controller;

import com.college.club.common.vo.LoginVO;
import com.college.club.common.vo.RegisterVO;
import com.college.club.common.vo.Result; // 你项目的统一返回类
import com.college.club.dto.ChangePasswordDTO;
import com.college.club.dto.SysUserLoginDTO;
import com.college.club.dto.SysUserRegisterDTO;
import com.college.club.dto.UserProfileUpdateDTO;
import com.college.club.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Tag(name = "个人中心", description = "注册、登录、退出登录接口")
@RestController
@RequestMapping("/api/user") // 接口统一前缀
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private HttpSession session;

    /**
     * 注册接口（公开访问，无需登录）
     */


    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<RegisterVO> register(@Valid @RequestBody SysUserRegisterDTO dto) {
        RegisterVO registerVO = sysUserService.register(dto);
        return Result.success(registerVO);
    }

    /**
     * 登录接口（公开访问，无需登录）
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody SysUserLoginDTO dto) {
        LoginVO loginVO = sysUserService.login(dto);
        return Result.success(loginVO);
    }

    /**
     * 退出登录接口（需要登录）
     */

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout() {
        session.removeAttribute("loginUser"); // 清除登录态
        return Result.success("退出登录成功");
    }


    @Operation(summary = "个人资料维护")
    @PostMapping("/updateProfile")
    public Result<String> updateProfile(@RequestBody UserProfileUpdateDTO profileDTO) {
        boolean updateSuccess = sysUserService.updateUserProfile(profileDTO);
        if (updateSuccess) {
            return Result.success("个人资料更新成功");
        }

        return (Result<String>) Result.failSystem("个人资料更新失败");

    }

    @Operation(summary = "密码修改")
    @PostMapping("/ChangePassword")
    public Result<?> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        boolean updateSuccess = sysUserService.ChangePassword(changePasswordDTO);
        if (updateSuccess) {
            return Result.success("密码修改成功，请重新输入");
        }

        return Result.failSystem("密码修改失败，请重试");
    }
}