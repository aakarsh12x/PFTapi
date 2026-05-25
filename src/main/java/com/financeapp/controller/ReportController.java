package com.financeapp.controller;

import com.financeapp.dto.FinancialSummaryDto;
import com.financeapp.dto.MonthlyReportDto;
import com.financeapp.dto.YearlyReportDto;
import com.financeapp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics", description = "Endpoints for aggregate financial calculations, reports, and charts data")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial reports summary", description = "Calculates and returns total income, expenses, current balance, monthly spending, category breakdown, and savings progress.")
    @ApiResponse(responseCode = "200", description = "Successfully compiled and retrieved financial summary")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<FinancialSummaryDto> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        FinancialSummaryDto response = reportService.getSummary(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "Get monthly financial report", description = "Retrieves category breakdown of income and expenses for the specified year and month.")
    @ApiResponse(responseCode = "200", description = "Successfully compiled and retrieved monthly report")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<MonthlyReportDto> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        MonthlyReportDto response = reportService.getMonthlyReport(year, month, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly/{year}")
    @Operation(summary = "Get yearly financial report", description = "Retrieves category breakdown of income and expenses for the specified year.")
    @ApiResponse(responseCode = "200", description = "Successfully compiled and retrieved yearly report")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<YearlyReportDto> getYearlyReport(
            @PathVariable int year,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        YearlyReportDto response = reportService.getYearlyReport(year, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
