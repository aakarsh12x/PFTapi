package com.financeapp.service;

import com.financeapp.dto.FinancialSummaryDto;

public interface ReportService {
    FinancialSummaryDto getSummary(String userEmail);
    com.financeapp.dto.MonthlyReportDto getMonthlyReport(int year, int month, String userEmail);
    com.financeapp.dto.YearlyReportDto getYearlyReport(int year, String userEmail);
}
