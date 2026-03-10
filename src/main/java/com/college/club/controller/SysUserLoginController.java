package com.college.club.controller;

import com.college.club.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.Serializable;

/**
 * 登录专属控制类（保证Swagger文档显示登录接口）
 * 单独分离，避免和其他接口混叠，Swagger扫描更清晰
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户登录接口", description = "用户登录相关操作（JSON格式传参）")
public class SysUserLoginController {

    /**
     * 登录请求参数DTO（仅用于Swagger文档生成，无需业务层处理）
     * 和前端JSON传参结构完全一致
     */
    @Schema(name = "LoginRequestDTO", description = "登录请求参数（JSON格式）")
    public static class LoginRequestDTO implements Serializable {
        @Schema(name = "username", description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "test_native")
        private String username;

        @Schema(name = "password", description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
        private String password;

        // Getter & Setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 登录接口（JSON格式传参）
     * 注：实际请求由Security原生过滤器处理，此接口仅用于Swagger文档生成
     */
    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "原生Security登录，支持JSON格式传参，登录成功后自动初始化上下文",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "登录示例",
                                    value = "{\"username\":\"test_native\",\"password\":\"123456\"}"
                            )
                    ),
                    required = true
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":200,\"msg\":\"success\",\"data\":\"登录成功\"}"))),
                    @ApiResponse(responseCode = "400", description = "登录失败（用户名/密码错误/账号禁用）", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"fail\",\"data\":\"用户名或密码错误\"}"))),
                    @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":401,\"msg\":\"fail\",\"data\":\"请先登录后再操作\"}")))
            }
    )
    public Result<String> login(
            @Valid @RequestBody
            @Parameter(hidden = true) // 隐藏Swagger的参数显示，避免重复
            LoginRequestDTO loginRequestDTO
    ) {
        // 注：此方法体不会实际执行，因为请求会先被Security的JSON过滤器拦截处理
        // 仅用于Swagger生成文档，返回值为示例格式
        return Result.success("登录成功");
    }
}