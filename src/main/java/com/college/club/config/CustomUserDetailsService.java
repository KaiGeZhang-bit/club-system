package com.college.club.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.college.club.common.exception.BusinessException;
import com.college.club.entity.SysUser;
import com.college.club.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. 根据用户名查询用户（你的原业务逻辑）
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        SysUser user = sysUserMapper.selectOne(queryWrapper);

        // 2. 校验用户是否存在（抛你的自定义异常，替代原生异常）
        if (user == null) {
            throw BusinessException.businessError("用户名或密码错误");
        }

        // 3. 校验账号状态（1=正常，0=禁用）（你的原业务逻辑）
        if (user.getStatus() != 1) {
            throw BusinessException.businessError("账号已禁用，请联系管理员");
        }

        // 4. 封装UserDetails，原生流程会自动用你PasswordConfig的BCrypt编码器比对密码
        // 无需手动比对密码，原生流程统一处理（保证加密/比对规则一致）
        return new User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList() // 权限暂时空，后续加角色可在这里配置
        );
    }
}