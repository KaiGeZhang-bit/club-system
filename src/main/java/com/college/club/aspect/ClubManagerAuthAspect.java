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

@Aspect
@Component
public class ClubManagerAuthAspect {

    @Autowired
    private ClubInfoService clubInfoService;
    @Autowired
    private ClubAnnouncementService announcementService;
    @Autowired
    private SysUserService sysUserService;

    @Pointcut("@annotation(com.college.club.annotation.ClubManagerAuth)")
    public void clubManagerAuthPointcut() {}

    @Around("clubManagerAuthPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        SysUser currentUser = sysUserService.getCurrentUser();
        if (currentUser == null) {
            return Result.failParam("请先登录后再操作");
        }

        Long currentUserId = currentUser.getId();
        Integer userRole = currentUser.getRole();

        if (userRole == null) {
            return Result.failBusiness("用户角色信息异常");
        }

        Long targetClubId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                Long announcementId = (Long) arg;
                ClubAnnouncement ann = announcementService.getById(announcementId);
                if (ann == null) {
                    return Result.failBusiness("公告不存在");
                }
                targetClubId = ann.getClubId();
            } else if (arg instanceof ClubAnnouncement) {
                targetClubId = ((ClubAnnouncement) arg).getClubId();
            }
            if (targetClubId != null) {
                break;
            }
        }

        if (targetClubId == null) {
            return Result.failBusiness("无法确定公告所属社团");
        }

        ClubInfo targetClub = clubInfoService.getById(targetClubId);
        if (targetClub == null) {
            return Result.failBusiness("社团不存在");
        }

        if (userRole == 2) {
            return joinPoint.proceed();
        }

        if (userRole == 1 && currentUserId.equals(targetClub.getLeaderId())) {
            return joinPoint.proceed();
        }

        return Result.failBusiness("权限不足：仅社团管理员、社团负责人可操作公告");
    }
}
