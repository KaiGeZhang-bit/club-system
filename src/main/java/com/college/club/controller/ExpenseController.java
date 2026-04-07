package com.college.club.controller;

import com.college.club.common.vo.ExpenseApplicationVO;
import com.college.club.common.vo.ExpenseTransactionVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ApprovalDTO;
import com.college.club.dto.ExpenseApplicationDTO;
import com.college.club.entity.SysUser;
import com.college.club.service.ExpenseApplicationService;
import com.college.club.service.ExpenseApprovalService;
import com.college.club.service.ExpenseTransactionService;
import com.college.club.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "经费管理")
@RestController
@RequestMapping("/api/expense")
public class ExpenseController {



    @Autowired
    private ExpenseApplicationService applicationService;

    @Autowired
    private ExpenseApprovalService approvalService;

    @Autowired
    private ExpenseTransactionService transactionService;
    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "提交经费申请")
    @PostMapping("/apply")
    public Result<Long> submitApplication(@Valid @RequestBody ExpenseApplicationDTO dto) {
        Long userId = sysUserService.getCurrentUser().getId();
        Long applicationId = applicationService.submitApplication(dto, userId);
        return Result.success(applicationId);
    }

    @Operation(summary = "分页查询申请列表")
    @GetMapping("/applications")
    public Result<PageVO<ExpenseApplicationVO>> queryApplications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageVO<ExpenseApplicationVO> page = applicationService.queryApplicationList(
                pageNum, pageSize, clubId, status, startTime, endTime);
        return Result.success(page);
    }

    @Operation(summary = "获取申请详情")
    @GetMapping("/application/{id}")
    public Result<ExpenseApplicationVO> getApplicationDetail(@PathVariable Long id) {
        ExpenseApplicationVO detail = applicationService.getApplicationDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "审批经费申请")
    @PutMapping("/application/{id}/approve")
    public Result<Void> approveApplication(@PathVariable Long id,
                                           @Valid @RequestBody ApprovalDTO dto) {
        Long approverId = sysUserService.getCurrentUser().getId();
        approvalService.approveApplication(id, dto, approverId);
        return (Result<Void>) Result.success();
    }

    @Operation(summary = "分页查询经费流水")
    @GetMapping("/transactions")
    public Result<PageVO<ExpenseTransactionVO>> queryTransactions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageVO<ExpenseTransactionVO> page = transactionService.queryTransactionList(
                pageNum, pageSize, clubId, type, startTime, endTime);
        return Result.success(page);
    }
}