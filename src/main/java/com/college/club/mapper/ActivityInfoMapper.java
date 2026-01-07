package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ActivityInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动Mapper接口，负责和数据库交互
 * 继承BaseMapper后，自带insert/delete/update/select等基础方法
 */
@Mapper // 标记为MyBatis的Mapper接口，Spring会自动扫描并创建代理类
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    // 这里可以写自定义SQL方法（比如复杂查询），基础CRUD用BaseMapper自带的即可
}