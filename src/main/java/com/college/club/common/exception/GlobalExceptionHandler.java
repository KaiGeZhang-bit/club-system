package com.college.club.common.exception;

import com.college.club.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // （原有方法保留）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleParamValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数格式错误";
        log.warn("参数校验失败：{}", msg);
        return Result.failParam(msg);
    }

    // 修改此处：调用Result.fail()
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage()); // 替换原new Result的写法
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleOtherException(Exception e) {
        log.error("系统异常：", e);
        return Result.failSystem("系统繁忙，请稍后再试");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleFaviconError(NoResourceFoundException e) {
        // 只打印普通日志，不抛系统异常
        log.info("浏览器请求/favicon.ico图标，但项目未配置，忽略该请求：{}", e.getMessage());
        return Result.success(); // 或返回空，不影响前端
    }
}