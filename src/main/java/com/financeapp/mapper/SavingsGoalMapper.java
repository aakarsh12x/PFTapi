package com.financeapp.mapper;

import com.financeapp.dto.SavingsGoalRequestDto;
import com.financeapp.dto.SavingsGoalResponseDto;
import com.financeapp.entity.SavingsGoal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SavingsGoalMapper {

    public SavingsGoal toEntity(SavingsGoalRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        java.time.LocalDate resolvedTargetDate = dto.getTargetDate() != null ? dto.getTargetDate() : dto.getDeadline();
        java.time.LocalDate resolvedStartDate = dto.getStartDate() != null ? dto.getStartDate() : java.time.LocalDate.now();
        BigDecimal resolvedCurrent = dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO;

        return SavingsGoal.builder()
                .goalName(dto.getGoalName())
                .targetAmount(dto.getTargetAmount())
                .currentAmount(resolvedCurrent)
                .targetDate(resolvedTargetDate)
                .startDate(resolvedStartDate)
                .build();
    }

    public SavingsGoalResponseDto toResponseDto(SavingsGoal entity, BigDecimal currentProgressVal) {
        if (entity == null) {
            return null;
        }

        BigDecimal percentage = BigDecimal.ZERO;
        if (entity.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = currentProgressVal.multiply(new BigDecimal(100))
                    .divide(entity.getTargetAmount(), 2, RoundingMode.HALF_UP);
            if (percentage.compareTo(BigDecimal.ZERO) < 0) {
                percentage = BigDecimal.ZERO;
            }
            if (percentage.compareTo(new BigDecimal(100)) > 0) {
                percentage = new BigDecimal(100);
            }
        }

        BigDecimal remaining = entity.getTargetAmount().subtract(currentProgressVal);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return SavingsGoalResponseDto.builder()
                .id(entity.getId())
                .goalName(entity.getGoalName())
                .targetAmount(entity.getTargetAmount())
                .targetDate(entity.getTargetDate())
                .startDate(entity.getStartDate())
                .currentProgress(currentProgressVal)
                .progressPercentage(percentage.doubleValue())
                .remainingAmount(remaining)
                .build();
    }
}
