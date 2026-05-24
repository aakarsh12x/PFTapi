package com.financeapp.service;

import com.financeapp.dto.SavingsGoalRequestDto;
import com.financeapp.dto.SavingsGoalResponseDto;

import java.util.List;

public interface SavingsGoalService {
    SavingsGoalResponseDto createGoal(SavingsGoalRequestDto requestDto, String userEmail);
    SavingsGoalResponseDto getGoal(Long id, String userEmail);
    SavingsGoalResponseDto updateGoal(Long id, SavingsGoalRequestDto requestDto, String userEmail);
    void deleteGoal(Long id, String userEmail);
    List<SavingsGoalResponseDto> getGoals(String userEmail);
}
