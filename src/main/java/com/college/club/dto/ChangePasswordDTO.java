package com.college.club.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class ChangePasswordDTO {

    @NotNull(message = "旧密码不能为空")
    private String oldPassword;
    @NotNull(message = "新密码不能为空")
    private String newPassword;
    @NotNull(message = "必须确认新密码")
    private String confirmPassword;


}
