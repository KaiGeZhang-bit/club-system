package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.LoginVO;
import com.college.club.common.vo.RegisterVO;
import com.college.club.dto.ChangePasswordDTO;
import com.college.club.dto.SysUserLoginDTO; // 你项目的登录DTO
import com.college.club.dto.SysUserRegisterDTO; // 你项目的注册DTO
import com.college.club.dto.UserProfileUpdateDTO;
import com.college.club.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    // 修正：参数用SysUserLoginDTO（和实现类一致）
    LoginVO login(SysUserLoginDTO dto);

    // 修正：参数用SysUserRegisterDTO（和实现类一致）
    RegisterVO register(SysUserRegisterDTO dto);

    // 获取当前登录用户
    SysUser getCurrentUser();

    //个人资料维护方法
    boolean updateUserProfile(UserProfileUpdateDTO profileDTO);

    boolean ChangePassword(ChangePasswordDTO changePasswordDTO);

}



