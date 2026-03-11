package com.college.club.aspect;

import com.college.club.annotation.ClubManagerAuth;
import com.college.club.entity.ClubAnnouncement;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.service.ClubAnnouncementService;
import com.college.club.service.ClubInfoService;
import com.college.club.service.SysUserService;
import com.college.club.common.vo.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 社团负责人权限校验切面（最终修正版）
 * 只做一件事：校验当前登录用户是否是公告所属社团的负责人
 */
@Aspect
@Component
public class ClubManagerAuthAspect {

    @Autowired
    private ClubInfoService clubInfoService;
    @Autowired
    private ClubAnnouncementService announcementService;
    @Autowired
    private SysUserService sysUserService;

    // 切入点：只拦截加了@ClubManagerAuth注解的方法
    @Pointcut("@annotation(com.college.club.annotation.ClubManagerAuth)")
    public void clubManagerAuthPointcut() {}

    @Around("clubManagerAuthPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取当前登录用户（从SysUserService拿真实登录用户）
        SysUser currentUser = sysUserService.getCurrentUser();
        if (currentUser == null) {
            return Result.failParam("请先登录后再操作");
        }
        Long currentUserId = currentUser.getId(); // 拿到用户ID（Long类型）

        // 2. 提取公告关联的社团ID（适配新增/修改/删除）
        Long targetClubId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) { // 删除/修改（传公告ID）
                ClubAnnouncement ann = announcementService.getById((Long) arg);
                if (ann == null) return Result.failBusiness("公告不存在");
                targetClubId = ann.getClubId();
            } else if (arg instanceof ClubAnnouncement) { // 新增/修改（传公告对象）
                targetClubId = ((ClubAnnouncement) arg).getClubId();
            }
            if (targetClubId != null) break; // 找到就退出，不浪费性能
        }

        // 3. 基础校验
        if (targetClubId == null) return Result.failBusiness("无法确定公告所属社团");
        ClubInfo targetClub = clubInfoService.getById(targetClubId);
        if (targetClub == null) return Result.failBusiness("社团不存在");

        // 4. 核心校验：用户ID == 社团leader_id（你要的直接对比）
        if (!currentUserId.equals(targetClub.getLeaderId())) {
            return Result.failBusiness("你不是该社团的负责人，无权操作公告");
        }

        // 5. 权限通过，放行执行业务逻辑
        return joinPoint.proceed();
    }
}