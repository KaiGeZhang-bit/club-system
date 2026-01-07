package com.college.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.college.club.entity.ActivityJoin;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper // 告诉代码：这是操作数据库的类
@Repository
public interface ActivityJoinMapper extends BaseMapper<ActivityJoin> {
    // 不用写任何代码！继承后自动有“增/删/改/查”功能
}