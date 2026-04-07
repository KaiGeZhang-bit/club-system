package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.Result;
import com.college.club.entity.ClubMessage;


import java.util.concurrent.CompletableFuture;

/**
 * 社团消息Service接口层（标准接口定义）
 */
public interface ClubMessageService extends IService<ClubMessage> {

    /**
     * 对外暴露的核心接口：管理员/社团负责人给本社团成员批量发消息（异步）
     * @param senderId 发送者ID（sys_user.id）
     * @param clubId 社团ID（club_info.id）
     * @param content 消息内容
     * @return 接口响应结果（立即返回，不阻塞）
     */
    Result<String> sendBatchMessage(Long senderId, Long clubId, String content);

    /**
     * 内部异步方法：实际执行批量插入消息的逻辑（由实现类实现）
     * @param senderId 发送者ID
     * @param clubId 社团ID
     * @param content 消息内容
     * @return CompletableFuture 异步结果
     */
    CompletableFuture<Void> asyncSendMessage(Long senderId, Long clubId, String content);

    /**
     * 内部权限校验方法：校验发送者是否有发消息的权限（由实现类实现）
     * @param senderId 发送者ID
     * @param clubId 社团ID
     * @return 权限校验结果
     */
    Result<String> checkSendPermission(Long senderId, Long clubId);
}