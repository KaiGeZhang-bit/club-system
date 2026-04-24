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
    @Autowired
    private com.college.club.service.ExpenseStatisticsService expenseStatisticsService;

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
    public Result<com.college.club.common.vo.TransactionPageVO> queryTransactions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        com.college.club.common.vo.TransactionPageVO page = transactionService.queryTransactionList(
                pageNum, pageSize, clubId, type, startTime, endTime);
        return Result.success(page);
    }

    @Operation(summary = "撤回经费申请")
    @PutMapping("/application/{id}/withdraw")
    public Result<Void> withdrawApplication(@PathVariable Long id) {
        applicationService.withdrawApplication(id);
        return (Result<Void>) Result.success();
    }

    @Operation(summary = "添加收入记录")
    @PostMapping("/income")
    public Result<Void> addIncome(
            @RequestParam Long clubId,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long activityId) {
        transactionService.addIncome(clubId, amount, source, description, activityId);
        return (Result<Void>) Result.success();
    }

    @Operation(summary = "查询社团经费统计")
    @GetMapping("/statistics")
    public Result<com.college.club.common.vo.ExpenseStatisticsVO> getStatistics(
            @RequestParam(required = false) Long clubId) {
        com.college.club.common.vo.ExpenseStatisticsVO statistics = 
            expenseStatisticsService.getClubStatistics(clubId);
        return Result.success(statistics);
    }

    @Operation(summary = "查询我负责的社团经费统计")
    @GetMapping("/myStatistics")
    public Result<com.college.club.common.vo.ExpenseStatisticsVO> getMyStatistics() {
        com.college.club.common.vo.ExpenseStatisticsVO statistics = 
            expenseStatisticsService.getMyManagedClubStatistics();
        return Result.success(statistics);
    }
}