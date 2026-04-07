package com.college.club.common.vo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ClubInfoVO {

    private Long id;
    private String clubName;
    private String clubType;
    private String intro;
    private Long leaderId;
    private String leaderName;
    private Long teacherId;
    private String teacherName;
    private Integer status;
    private String statusDesc;  //状态描述：0-待审核，1-正常，2-解散
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
