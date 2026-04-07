package com.college.club.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.vo.Result;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.ClubMessage;
import com.college.club.entity.SysUser;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.ClubMessageMapper;
import com.college.club.mapper.SysUserMapper;
import com.college.club.mapper.UserClubRelationMapper;
import com.college.club.service.ClubMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class) // 事务回滚，覆盖所有数据库操作
public class ClubMessageServiceImpl extends ServiceImpl<ClubMessageMapper, ClubMessage> implements ClubMessageService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private ClubInfoMapper clubInfoMapper;
    @Autowired
    private UserClubRelationMapper userClubRelationMapper;

    @Override
    public Result<String> sendBatchMessage(Long senderId, Long clubId, String content) {
        // 1. 参数合法性校验（匹配你的Result.failParam/400错误码）
        if (senderId == null || clubId == null || content == null || content.trim().isEmpty()) {
            return (Result<String>) Result.failParam("参数不能为空"); // 400参数错误
        }

        // 2. 调用权限校验方法（匹配你的Result.failBusiness/403错误码）
        Result<String> authResult = this.checkSendPermission(senderId, clubId);
        if (authResult.getCode() != 200) {
            return authResult; // 权限校验失败，直接返回（403/其他）
        }

        // 3. 调用异步发送方法（系统异常用failSystem/500错误码）
        try {
            CompletableFuture<Void> future = this.asyncSendMessage(senderId, clubId, content);
            return Result.success("消息发送任务已提交，系统将快速处理"); // 200成功
        } catch (Exception e) {
            log.error("提交异步消息发送失败", e);
            return (Result<String>) Result.failSystem("消息任务发送失败，请重试"); // 500系统错误
        }
    }

    @Override
    @Async("messageExecutor") // 指定自定义异步线程池
    public CompletableFuture<Void> asyncSendMessage(Long senderId, Long clubId, String content) {
        try {
            // 1. 查询该社团已加入的成员ID（status = 1）
            List<Long> memberIds = userClubRelationMapper.listJoinedMemberIds(clubId);
            if (memberIds == null || memberIds.isEmpty()) {
                log.warn("社团ID{}暂无已加入成员，无需发送消息", clubId);
                return CompletableFuture.completedFuture(null);
            }

            // 2. 查询社团名称（冗余存储，补充空指针防护）
            ClubInfo clubInfo = clubInfoMapper.selectById(clubId);
            if (clubInfo == null) {
                log.error("社团ID{}不存在，无法获取社团名称", clubId);
                throw new RuntimeException("社团不存在，无法发送消息");
            }
            String clubName = clubInfo.getClubName();

            // 3. 构造批量插入的消息列表
            List<ClubMessage> messageList = new ArrayList<>();
            Date now = new Date();
            for (Long receiverId : memberIds) {
                ClubMessage message = new ClubMessage();
                message.setReceiverId(receiverId);
                message.setClubId(clubId);
                message.setClubName(clubName);
                message.setContent(content);
                message.setIsRead(0); // 初始未读
                message.setCreateTime(now);
                messageList.add(message);
            }

            // 4. MyBatis-Plus批量插入（批次500，避免内存溢出）
            boolean isSuccess = this.saveBatch(messageList, 500);
            if (isSuccess) {
                log.info("异步发送消息成功！发送者ID{}，社团ID{}，接收人数{}", senderId, clubId, memberIds.size());
            } else {
                log.error("异步发送消息失败！发送者ID{}，社团ID{}", senderId, clubId);
                throw new RuntimeException("批量插入消息失败");
            }
        } catch (Exception e) {
            log.error("异步消息发送异常", e);
            throw new RuntimeException("异步发送消息失败", e); // 触发事务回滚
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Result<String> checkSendPermission(Long senderId, Long clubId) {
        // 1. 校验发送者是否存在
        SysUser sender = sysUserMapper.selectById(senderId);
        if (sender == null) {
            return (Result<String>) Result.failBusiness("发送者不存在！"); // 403业务错误
        }

        // 2. 获取用户角色，精准权限校验
        Integer userRole = sender.getRole();
        if (userRole == null) {
            return (Result<String>) Result.failBusiness("用户角色未配置！"); // 403业务错误
        }

        // 2.1 普通成员（role=0）：无权限
        if (userRole == 0) {
            return (Result<String>) Result.failBusiness("权限不够，仅管理员/社团负责人可发送消息"); // 403
        }

        // 2.2 老师/全局管理员（role=2）：直接放行
        if (userRole == 2) {
            return Result.success("权限校验通过"); // 200
        }

        // 2.3 社团负责人（role=1）：校验是否是当前社团的负责人
        if (userRole == 1) {
            ClubInfo clubInfo = clubInfoMapper.selectById(clubId);
            if (clubInfo == null) {
                return (Result<String>) Result.failBusiness("社团不存在"); // 403
            }
            if (!senderId.equals(clubInfo.getLeaderId())) {
                return (Result<String>) Result.failBusiness("你只能发送自己负责的社团消息"); // 403
            }
            return Result.success("权限校验通过"); // 200
        }

        // 2.4 未知角色：兜底提示
        return (Result<String>) Result.failBusiness("用户角色为" + userRole + "，无发送消息权限"); // 403
    }
}