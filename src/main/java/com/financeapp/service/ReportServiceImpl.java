package com.financeapp.service;

import com.financeapp.dto.CategoryBreakdownDto;
import com.financeapp.dto.FinancialSummaryDto;
import com.financeapp.dto.SavingsGoalResponseDto;
import com.financeapp.dto.MonthlyReportDto;
import com.financeapp.dto.YearlyReportDto;
import com.financeapp.entity.User;
import com.financeapp.entity.Transaction;
import com.financeapp.enums.TransactionType;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.mapper.SavingsGoalMapper;
import com.financeapp.repository.UserRepository;
import com.financeapp.repository.TransactionRepository;
import com.financeapp.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    @Override
    @Transactional(readOnly = true)
    public FinancialSummaryDto getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndType(user, TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserAndType(user, TransactionType.EXPENSE);
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // Calculate current month spending (expenses)
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal monthlySpending = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user, TransactionType.EXPENSE, startOfMonth, endOfMonth
        );

        // Get category-wise breakdown for expenses
        List<CategoryBreakdownDto> categoryBreakdown = transactionRepository.getCategoryBreakdown(user, TransactionType.EXPENSE)
                .stream()
                .map(projection -> CategoryBreakdownDto.builder()
                        .category(projection.getCategory())
                        .totalAmount(projection.getAmount())
                        .build())
                .collect(Collectors.toList());

        // Get savings goals progress
        List<SavingsGoalResponseDto> savingsGoals = savingsGoalRepository.findByUser(user)
                .stream()
                .map(g -> {
                    BigDecimal progress = transactionRepository.calculateSavingsSince(user, g.getStartDate());
                    return savingsGoalMapper.toResponseDto(g, progress);
                })
                .collect(Collectors.toList());

        return FinancialSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .balance(balance)
                .monthlySpending(monthlySpending)
                .categoryBreakdown(categoryBreakdown)
                .savingsGoals(savingsGoals)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportDto getMonthlyReport(int year, int month, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, start, end);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome.put(t.getCategory(), totalIncome.getOrDefault(t.getCategory(), BigDecimal.ZERO).add(t.getAmount()));
                sumIncome = sumIncome.add(t.getAmount());
            } else {
                totalExpenses.put(t.getCategory(), totalExpenses.getOrDefault(t.getCategory(), BigDecimal.ZERO).add(t.getAmount()));
                sumExpenses = sumExpenses.add(t.getAmount());
            }
        }

        return MonthlyReportDto.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(sumIncome.subtract(sumExpenses))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public YearlyReportDto getYearlyReport(int year, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, start, end);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome.put(t.getCategory(), totalIncome.getOrDefault(t.getCategory(), BigDecimal.ZERO).add(t.getAmount()));
                sumIncome = sumIncome.add(t.getAmount());
            } else {
                totalExpenses.put(t.getCategory(), totalExpenses.getOrDefault(t.getCategory(), BigDecimal.ZERO).add(t.getAmount()));
                sumExpenses = sumExpenses.add(t.getAmount());
            }
        }

        return YearlyReportDto.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(sumIncome.subtract(sumExpenses))
                .build();
    }
}
