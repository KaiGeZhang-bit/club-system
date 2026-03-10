package com.college.club.common.vo;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    // 私有构造方法（仅内部使用）
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // （原有快捷方法保留）
    public static Result<?> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static Result<?> failParam(String msg) {
        return new Result<>(400, msg, null);
    }

    public static Result<?> failBusiness(String msg) {
        return new Result<>(403, msg, null);
    }

    public static Result<?> failSystem(String msg) {
        return new Result<>(500, msg, null);
    }

    // 新增：支持自定义错误码的失败方法（用于业务异常）
    public static Result<?> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }


}