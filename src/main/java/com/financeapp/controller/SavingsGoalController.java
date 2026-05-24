package com.financeapp.controller;

import com.financeapp.dto.SavingsGoalRequestDto;
import com.financeapp.dto.SavingsGoalResponseDto;
import com.financeapp.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/savings-goals", "/api/goals"})
@RequiredArgsConstructor
@Tag(name = "Savings Goals", description = "Endpoints for managing financial savings goals and tracking target progress")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    @Operation(summary = "Create savings goal", description = "Creates a new savings goal for the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Savings goal successfully created")
    @ApiResponse(responseCode = "400", description = "Validation error on request body")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<SavingsGoalResponseDto> createGoal(
            @Valid @RequestBody SavingsGoalRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SavingsGoalResponseDto response = savingsGoalService.createGoal(requestDto, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all savings goals", description = "Retrieves all savings goals for the current authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved goals list")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<Map<String, Object>> getGoals(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<SavingsGoalResponseDto> goals = savingsGoalService.getGoals(userDetails.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("goals", goals);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get savings goal by ID", description = "Retrieves a single savings goal owned by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved goal")
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized to access this savings goal")
    @ApiResponse(responseCode = "404", description = "Goal not found")
    public ResponseEntity<SavingsGoalResponseDto> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SavingsGoalResponseDto response = savingsGoalService.getGoal(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update savings goal", description = "Updates fields of an existing savings goal owned by the user.")
    @ApiResponse(responseCode = "200", description = "Savings goal successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error on update payload")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized to update this savings goal")
    @ApiResponse(responseCode = "404", description = "Goal not found")
    public ResponseEntity<SavingsGoalResponseDto> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SavingsGoalResponseDto response = savingsGoalService.updateGoal(id, requestDto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete savings goal", description = "Deletes a savings goal owned by the user.")
    @ApiResponse(responseCode = "200", description = "Savings goal successfully deleted")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized to delete this savings goal")
    @ApiResponse(responseCode = "404", description = "Goal not found")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        savingsGoalService.deleteGoal(id, userDetails.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Goal deleted successfully");
        return ResponseEntity.ok(response);
    }
}
