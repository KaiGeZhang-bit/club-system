package com.college.club.controller;

import com.college.club.entity.ActivityInfo;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.service.ActivityInfoService;
import com.college.club.service.ClubInfoService;
import com.college.club.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController // 返回JSON格式数据
@RequestMapping("/test") // 接口统一前缀
public class TestController {

    // 注入需要测试的Service
    @Autowired
    private ActivityInfoService activityInfoService;
    @Autowired
    private ClubInfoService clubInfoService;



    // 1. 测试：查询所有活动列表（GET请求）
    @GetMapping("/activity/list")
    public List<ActivityInfo> getActivityList() {
        // 调用Service的list()方法（MyBatis-Plus自带的查询所有方法）
        return activityInfoService.list();
    }


    // 2. 测试：根据ID查询社团详情（GET请求）
    @GetMapping("/club/get/{id}")
    public ClubInfo getClubById(@PathVariable Long id) {
        // 调用Service的getById()方法（根据主键查询）
        return clubInfoService.getById(id);
    }


    // 3. 测试：新增用户（POST请求，这里为了测试方便用GET，实际开发用POST）
    @GetMapping("/user/add")
    public String addTestUser() {
        SysUser user = new SysUser();
        user.setUsername("test_user_001"); // 测试用户名
        user.setPassword("123456"); // 实际项目要加密，这里测试用明文
        user.setName("测试用户");
        user.setRole(0); // 0=普通成员
        user.setStatus(1); // 1=正常
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return "当前功能暂未启用";
    }

}