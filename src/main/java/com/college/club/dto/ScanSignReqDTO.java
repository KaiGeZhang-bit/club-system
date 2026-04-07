package com.college.club.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScanSignReqDTO {

    //二维码原始内容
    @NotBlank(message = "二维码内容不能为空")
    private String qrContent;


    //签到Ip
    private String SignIp;


}
