package com.college.club.controller;

import com.college.club.common.vo.BudgetVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.BudgetDTO;
import com.college.club.service.ClubBudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "预算管理")
@RestController
@RequestMapping("/api/budget")
public class ClubBudgetController {

    @Autowired
    private ClubBudgetService budgetService;

    @Operation(summary = "设置预算")
    @PostMapping("/set")
    public Result<Void> setBudget(@Valid @RequestBody BudgetDTO dto) {
        budgetService.setBudget(dto);
        return (Result<Void>) Result.success();
    }

    @Operation(summary = "查询预算详情")
    @GetMapping("/detail")
    public Result<BudgetVO> getBudget(
            @RequestParam Long clubId,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer quarter) {
        BudgetVO budget = budgetService.getBudget(clubId, year, quarter);
        return Result.success(budget);
    }

    @Operation(summary = "查询预算列表")
    @GetMapping("/list")
    public Result<List<BudgetVO>> getBudgetList(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Integer year) {
        List<BudgetVO> list = budgetService.getBudgetList(clubId, year);
        return Result.success(list);
    }
}
