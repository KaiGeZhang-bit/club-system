package com.college.club.controller;

import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.Result;
import com.college.club.entity.SysUser;
import com.college.club.service.ClubMessageService;
import com.college.club.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/club/message")
public class ClubMessageController {

    @Autowired
    private ClubMessageService clubMessageService;
    @Autowired
    private SysUserService sysUserService; // 注入用户服务

    @Operation(summary = "批量发送消息给社团成员")
    @PostMapping("/sendBatch")
    public Result<String> sendBatchMessage(
            @Parameter(description = "社团ID") @RequestParam Long clubId,
            @Parameter(description = "消息内容") @RequestParam String content) {

        // 1. 直接调用你已有的getCurrentUser方法（复用代码，无需重复写上下文逻辑）
        SysUser currentUser = sysUserService.getCurrentUser();
        Long senderId = currentUser.getId(); // 发送者ID

        log.info("当前登录用户ID：{}，发起社团{}消息发送请求，内容：{}", senderId, clubId, content);

        // 2. 调用消息发送服务
        return clubMessageService.sendBatchMessage(senderId, clubId, content);
    }

    @Operation(summary = "查询我发送过的消息列表（分页）")
    @GetMapping("/sent")
    public Result<PageVO<?>> getSentMessages(
            @Parameter(description = "页码，默认1") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数，默认10") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "社团ID（可选）") @RequestParam(required = false) Long clubId) {
        
        return (Result) clubMessageService.getSentMessages(pageNum, pageSize, clubId);
    }

    @Operation(summary = "查询我收到的消息列表（分页）")
    @GetMapping("/received")
    public Result<PageVO<?>> getReceivedMessages(
            @Parameter(description = "页码，默认1") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数，默认10") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "是否已读：0=未读，1=已读（可选）") @RequestParam(required = false) Integer isRead) {
        
        return (Result) clubMessageService.getReceivedMessages(pageNum, pageSize, isRead);
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/read/{messageId}")
    public Result<String> markAsRead(
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        
        return clubMessageService.markAsRead(messageId);
    }
}