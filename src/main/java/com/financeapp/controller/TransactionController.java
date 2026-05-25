package com.financeapp.controller;

import com.financeapp.dto.TransactionRequestDto;
import com.financeapp.dto.TransactionResponseDto;
import com.financeapp.enums.TransactionType;
import com.financeapp.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for managing transactions (income and expense)")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create transaction", description = "Creates a new transaction for the authenticated user context.")
    @ApiResponse(responseCode = "201", description = "Transaction successfully created")
    @ApiResponse(responseCode = "400", description = "Validation error on transaction request body")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TransactionResponseDto response = transactionService.createTransaction(requestDto, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Updates fields of an existing transaction owned by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Transaction successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error on update payload")
    @ApiResponse(responseCode = "403", description = "Not authorized to update this transaction")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TransactionResponseDto response = transactionService.updateTransaction(id, requestDto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Removes a transaction owned by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Transaction successfully deleted")
    @ApiResponse(responseCode = "403", description = "Not authorized to delete this transaction")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        transactionService.deleteTransaction(id, userDetails.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated, filtered transactions", description = "Retrieves a list/page of transactions for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction page")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String resolvedSortBy = sortBy;
        if ("transactionDate".equals(sortBy) || "date".equals(sortBy)) {
            resolvedSortBy = "date";
        }
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(resolvedSortBy).ascending() : Sort.by(resolvedSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponseDto> result = transactionService.getTransactions(
                userDetails.getUsername(), category, type, startDate, endDate, pageable
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", result.getContent());
        response.put("content", result.getContent());
        response.put("totalPages", result.getTotalPages());
        response.put("totalElements", result.getTotalElements());
        response.put("last", result.isLast());
        
        Map<String, Object> pageableMap = new HashMap<>();
        pageableMap.put("pageNumber", result.getNumber());
        pageableMap.put("pageSize", result.getSize());
        response.put("pageable", pageableMap);
        
        return ResponseEntity.ok(response);
    }
}
