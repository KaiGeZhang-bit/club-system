package com.college.club.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 所有业务相关的错误都抛这个异常（如“社团已解散”“报名已结束”）
 */
@Getter // Lombok生成getter方法，方便全局异常处理器获取信息
public class BusinessException extends RuntimeException {
    // 错误码（和Result的code对应）
    private final int code;

    // 构造方法：传入错误码和提示信息
    public BusinessException(int code, String message) {
        super(message); // 父类RuntimeException的message
        this.code = code;
    }

    // -------------- 快捷创建方法（不用手动写code）--------------
    // 参数错误（对应Result的400）
    public static BusinessException paramError(String message) {
        return new BusinessException(400, message);
    }

    // 业务错误（对应Result的403）
    public static BusinessException businessError(String message) {
        return new BusinessException(403, message);
    }
}