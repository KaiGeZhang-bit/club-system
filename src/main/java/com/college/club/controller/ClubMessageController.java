package com.college.club.controller;

import com.college.club.common.vo.Result;
import com.college.club.entity.SysUser;
import com.college.club.service.ClubMessageService;
import com.college.club.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/club/message")
public class ClubMessageController {

    @Autowired
    private ClubMessageService clubMessageService;
    @Autowired
    private SysUserService sysUserService; // 注入用户服务

    @PostMapping("/sendBatch")
    public Result<String> sendBatchMessage(
            @RequestParam Long clubId,
            @RequestParam String content) {

        // 1. 直接调用你已有的getCurrentUser方法（复用代码，无需重复写上下文逻辑）
        SysUser currentUser = sysUserService.getCurrentUser();
        Long senderId = currentUser.getId(); // 发送者ID

        log.info("当前登录用户ID：{}，发起社团{}消息发送请求，内容：{}", senderId, clubId, content);

        // 2. 调用消息发送服务
        return clubMessageService.sendBatchMessage(senderId, clubId, content);
    }
}