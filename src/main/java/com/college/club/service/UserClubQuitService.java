package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubQuitAuditDTO;
import com.college.club.dto.QuitClubDTO;
import com.college.club.entity.UserClubQuit;


public interface UserClubQuitService extends IService<UserClubQuit> {
    Result<?> QuitClub(QuitClubDTO quitClubDTO);
    Result<?> ClubQuitAudit(ClubQuitAuditDTO clubQuitAuditDTO);
    // 分页查询我的退出申请
    Result<?> getMyQuitApplyList(Integer pageNum, Integer pageSize);

    //撤销我的推出社团申请
    Result<?> withdrawQuitApply(Long quitApplyId);

}
