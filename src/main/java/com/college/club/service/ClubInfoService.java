package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.common.vo.ClubInfoVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubAuditDTO;
import com.college.club.dto.CreateClubDTO;
import com.college.club.entity.ClubInfo;

public interface ClubInfoService extends IService<ClubInfo> {
    Result<?> createClub(CreateClubDTO dto, Long userId);
    Result<String> auditClub(ClubAuditDTO dto);
    //修改社团信息
    Result<?> updateClub(CreateClubDTO dto);

    Result<PageVO<ClubInfoVO>> getClubList(Integer status, Integer pageNum, Integer pageSize);

    Result<ClubInfoVO> getClubDetail(Long id);

    Result<PageVO<ClubInfoVO>> getAuditList(Integer pageNum, Integer pageSize);

    Result<PageVO<ClubInfoVO>> getMyClubs(Integer pageNum, Integer pageSize);

    Result<?> getMyManageClubs();
}