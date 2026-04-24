package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException; // 用你项目里的异常类
import com.college.club.common.vo.RegisterVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.UserAuditDTO;
import com.college.club.dto.ChangePasswordDTO;
import com.college.club.dto.SysUserRegisterDTO; // 你项目的注册DTO
import com.college.club.dto.UserProfileUpdateDTO;
import com.college.club.entity.SysUser;
import com.college.club.mapper.SysUserMapper;
import com.college.club.service.SysUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    // 注入用户Mapper（操作sys_user表）
    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;


//    /**
//     * 登录功能实现：账号密码校验 → 存储Session → 返回用户信息
//     */
//    @Override
//    public LoginVO login(SysUserLoginDTO dto) {
//
//        // 1. 根据用户名查询用户（sys_user表）
//        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("username", dto.getUsername());
//        SysUser user = sysUserMapper.selectOne(queryWrapper);
//
//        // 2. 校验用户是否存在
//        if (user == null) {
//            throw BusinessException.businessError("用户名或密码错误");
//        }
//
//        // 3. 校验账号状态是否正常（1=正常，0=禁用）
//        if (user.getStatus() != 1) {
//            throw BusinessException.businessError("账号已禁用，请联系管理员");
//        }
//
//        // 4. 校验密码（前端传明文，后端与加密后的密码比对）
//        if (!bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
//            throw BusinessException.businessError("用户名或密码错误");
//        }
//
//
//        // 6. 构建登录成功返回结果（LoginVO）
//        LoginVO loginVO = new LoginVO();
//        loginVO.setUserId(user.getId());
//        loginVO.setUsername(user.getUsername());
//        loginVO.setName(user.getName());
//        loginVO.setRole(user.getRole());
//        loginVO.setMessage("登录成功");
//
//        return loginVO;
//    }

    /**
     * 注册功能实现：校验合法性 → 密码加密 → 保存用户
     */
    @Override

    public RegisterVO register(SysUserRegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw BusinessException.businessError("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty() || dto.getPassword().length() < 6) {
            throw BusinessException.businessError("密码不能为空，且长度不能少于6位");
        }

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername().trim());
        if (sysUserMapper.selectCount(queryWrapper) > 0) {
            throw BusinessException.businessError("用户名已被占用，请更换账号");
        }

        if (dto.getRole() != 0 && dto.getRole() != 1 && dto.getRole() != 2) {
            throw BusinessException.businessError("角色非法，仅支持学生（0）、社团负责人（1）、老师（2）");
        }

        String encryptedPassword = bCryptPasswordEncoder.encode(dto.getPassword().trim());

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(encryptedPassword);
        user.setName(dto.getName() != null ? dto.getName().trim() : null);
        user.setRole(dto.getRole());

        if (dto.getRole() == 0) {
            user.setStatus(1);
        } else {
            user.setStatus(0);
        }

        user.setCreateTime(LocalDateTime.now());

        sysUserMapper.insert(user);

        RegisterVO registerVO = new RegisterVO();
        registerVO.setUserId(user.getId());
        registerVO.setUsername(user.getUsername());
        registerVO.setName(user.getName());
        registerVO.setRole(user.getRole());

        if (dto.getRole() == 0) {
            registerVO.setMessage("注册成功，请登录");
        } else {
            registerVO.setMessage("注册申请已提交，等待老师审核通过后即可登录");
        }

        return registerVO;
    }
    /**
     * 获取当前登录用户（供后续审核功能使用，无需前端传ID）
     */
    @Override
    public SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            throw BusinessException.businessError("请先登录");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        SysUser loginUser = sysUserMapper.selectOne(queryWrapper);
        if (loginUser == null) {
            throw BusinessException.businessError("请先登录");
        }
        return loginUser;
    }


    @Override
    public boolean updateUserProfile(UserProfileUpdateDTO profileDTO){
        SysUser CurrentUser = this.getCurrentUser();
        if(CurrentUser == null){
            throw BusinessException.businessError("该用户不存在，无法更行个人资料");
        }

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, CurrentUser.getId())
                .set(profileDTO.getName() != null,SysUser::getName, profileDTO.getName())
                .set(profileDTO.getPhone() != null,SysUser::getPhone , profileDTO.getPhone())
                .set(profileDTO.getAvatar() != null,SysUser::getAvatar, profileDTO.getAvatar());

        return this.update(updateWrapper);
    }


    @Override
    public boolean ChangePassword(ChangePasswordDTO ChangePasswordDTO) {
        SysUser CurrentUser = this.getCurrentUser();
        if(CurrentUser == null){
            throw BusinessException.businessError("用户不存在，无话修改密码");
        }

        String dbEncryptedPassword = getCurrentUser().getPassword();

        boolean isOldPassword = bCryptPasswordEncoder.matches(ChangePasswordDTO.getOldPassword(), dbEncryptedPassword);

        if(!isOldPassword){
            throw BusinessException.businessError("原密码输入错误，请重新输入！！！");
        }
        if(ChangePasswordDTO.getNewPassword() == null || !ChangePasswordDTO.getNewPassword().equals(ChangePasswordDTO.getConfirmPassword())){
            throw BusinessException.businessError("新密码与确认密码不一致，请重新输入！！！");

        }

        boolean isNewPassword = bCryptPasswordEncoder.matches(ChangePasswordDTO.getNewPassword(), dbEncryptedPassword);
        if(isNewPassword){
            throw BusinessException.businessError("新密码不能与原密码一致，请重新输入！！！");

        }

        String encryptedPassword = bCryptPasswordEncoder.encode(ChangePasswordDTO.getNewPassword());
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, CurrentUser.getId())
                .set(SysUser::getPassword, encryptedPassword);



        return this.update(updateWrapper);

    }


    @Override
    public Result<?> auditUser(UserAuditDTO dto) {
        SysUser currentUser = this.getCurrentUser();

        if (currentUser.getRole() != 2) {
            throw BusinessException.businessError("只有老师才能审核用户注册申请");
        }

        SysUser targetUser = sysUserMapper.selectById(dto.getUserId());
        if (targetUser == null) {
            throw BusinessException.businessError("用户不存在");
        }

        if (targetUser.getStatus() != 0) {
            throw BusinessException.businessError("该用户已审核，无需重复操作");
        }

        if (targetUser.getRole() == 0) {
            throw BusinessException.businessError("学生账号无需审核");
        }

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, dto.getUserId());

        if (dto.getAuditResult() == 1) {
            updateWrapper.set(SysUser::getStatus, 1);
            sysUserMapper.update(null, updateWrapper);
            return Result.success("审核通过，该用户现已可以登录");
        } else if (dto.getAuditResult() == 2) {
            updateWrapper.set(SysUser::getStatus, 2);
            sysUserMapper.update(null, updateWrapper);
            return Result.success("审核拒绝，该用户无法登录");
        } else {
            throw BusinessException.businessError("审核结果参数错误，1=通过，2=拒绝");
        }
    }

    @Override
    public Result<?> getPendingUsers(Integer pageNum, Integer pageSize) {
        SysUser currentUser = this.getCurrentUser();

        if (currentUser.getRole() != 2) {
            throw BusinessException.businessError("只有老师才能查看待审核用户列表");
        }

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role", 1, 2)
                .eq("status", 0)
                .orderByDesc("create_time");

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> resultPage =
                sysUserMapper.selectPage(page, queryWrapper);

        return Result.success(resultPage);
    }
}
