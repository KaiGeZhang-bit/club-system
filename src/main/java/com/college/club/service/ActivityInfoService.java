package com.college.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.college.club.dto.ActivityDTO;
import com.college.club.entity.ActivityInfo;
import com.college.club.common.vo.Result;
import com.college.club.common.vo.ActivityVO;
import java.util.List;

/**
 * 活动管理Service接口
 * 继承IService后，自带基础CRUD方法（如save/remove/list），再扩展自定义业务方法
 */
public interface ActivityInfoService extends IService<ActivityInfo> {
    // 1. 创建活动（默认草稿状态）
    Result<ActivityVO> createActivity(ActivityDTO dto);

    // 2. 查询活动列表（支持按状态、社团筛选）
    Result<List<ActivityVO>> getActivityList(Integer status, Long clubId);

    // 3. 查询活动详情（按ID）
    Result<ActivityVO> getActivityById(Long id);

    // 4. 更新活动状态（1-发布，2-下架）
    Result<?> updateActivityStatus(Long id, Integer status);

    // 5. 删除活动（仅草稿状态可删）
    Result<?> deleteActivity(Long id);

    //老师审核活动
    Result<?> auditActivity(Long id, Integer status);
}