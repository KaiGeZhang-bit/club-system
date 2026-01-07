package com.college.club.common.vo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyJoinApplyVO {

    private Long id;


    private Long userId;


    private Long clubId;


    private LocalDateTime joinTime;


    private Integer status;


    private LocalDateTime auditTime;


    private String clubName; // 社团名称


    private String statusDesc; // 状态描述


}
