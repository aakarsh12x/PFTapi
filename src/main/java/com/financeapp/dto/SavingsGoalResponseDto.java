package com.financeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalResponseDto {
    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    
    private LocalDate targetDate;
    private LocalDate startDate;
    private ProgressDetailsDto currentProgress;

    // Backwards compatibility with frontend client
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private double progressPercentage;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProgressDetailsDto {
        private BigDecimal percentageCompletion;
        private BigDecimal remainingAmount;
    }
}
