package com.financeapp.service;

import com.financeapp.dto.SavingsGoalRequestDto;
import com.financeapp.dto.SavingsGoalResponseDto;
import com.financeapp.entity.SavingsGoal;
import com.financeapp.entity.User;
import com.financeapp.exception.ForbiddenException;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.mapper.SavingsGoalMapper;
import com.financeapp.repository.UserRepository;
import com.financeapp.repository.SavingsGoalRepository;
import com.financeapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    @Override
    @Transactional
    public SavingsGoalResponseDto createGoal(SavingsGoalRequestDto requestDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        LocalDate resolvedTargetDate = requestDto.getTargetDate() != null ? requestDto.getTargetDate() : requestDto.getDeadline();
        if (resolvedTargetDate == null) {
            throw new IllegalArgumentException("Target date/Deadline is required");
        }
        if (!resolvedTargetDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be a future date");
        }

        LocalDate resolvedStartDate = requestDto.getStartDate() != null ? requestDto.getStartDate() : LocalDate.now();

        if (!resolvedStartDate.isBefore(resolvedTargetDate)) {
            throw new IllegalArgumentException("Start date must be before target date");
        }

        SavingsGoal goal = savingsGoalMapper.toEntity(requestDto);
        goal.setUser(user);
        goal.setTargetDate(resolvedTargetDate);
        goal.setStartDate(resolvedStartDate);

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);
        
        // Progress since start date is 0 initially for a new goal if no transactions exist,
        // but we can query it dynamically to be safe.
        BigDecimal currentProgress = transactionRepository.calculateSavingsSince(user, resolvedStartDate);

        return savingsGoalMapper.toResponseDto(savedGoal, currentProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalResponseDto getGoal(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to access this savings goal");
        }

        BigDecimal currentProgress = transactionRepository.calculateSavingsSince(user, goal.getStartDate());
        return savingsGoalMapper.toResponseDto(goal, currentProgress);
    }

    @Override
    @Transactional
    public SavingsGoalResponseDto updateGoal(Long id, SavingsGoalRequestDto requestDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to update this savings goal");
        }

        if (requestDto.getGoalName() != null) {
            goal.setGoalName(requestDto.getGoalName());
        }
        if (requestDto.getTargetAmount() != null) {
            if (requestDto.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Target amount must be positive");
            }
            goal.setTargetAmount(requestDto.getTargetAmount());
        }
        
        LocalDate resolvedTargetDate = requestDto.getTargetDate() != null ? requestDto.getTargetDate() : requestDto.getDeadline();
        LocalDate resolvedStartDate = requestDto.getStartDate();

        if (resolvedTargetDate != null) {
            if (!resolvedTargetDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Target date must be a future date");
            }
            LocalDate effectiveStart = (resolvedStartDate != null) ? resolvedStartDate : goal.getStartDate();
            if (effectiveStart != null && !effectiveStart.isBefore(resolvedTargetDate)) {
                throw new IllegalArgumentException("Start date must be before target date");
            }
            goal.setTargetDate(resolvedTargetDate);
        }

        if (resolvedStartDate != null) {
            LocalDate effectiveTarget = (resolvedTargetDate != null) ? resolvedTargetDate : goal.getTargetDate();
            if (effectiveTarget != null && !resolvedStartDate.isBefore(effectiveTarget)) {
                throw new IllegalArgumentException("Start date must be before target date");
            }
            goal.setStartDate(resolvedStartDate);
        }

        if (requestDto.getCurrentAmount() != null) {
            goal.setCurrentAmount(requestDto.getCurrentAmount());
        }

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        BigDecimal currentProgress = transactionRepository.calculateSavingsSince(user, updatedGoal.getStartDate());

        return savingsGoalMapper.toResponseDto(updatedGoal, currentProgress);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to delete this savings goal");
        }

        savingsGoalRepository.delete(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponseDto> getGoals(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<SavingsGoal> goals = savingsGoalRepository.findByUser(user);
        return goals.stream()
                .map(g -> {
                    BigDecimal currentProgress = transactionRepository.calculateSavingsSince(user, g.getStartDate());
                    return savingsGoalMapper.toResponseDto(g, currentProgress);
                })
                .collect(Collectors.toList());
    }
}
