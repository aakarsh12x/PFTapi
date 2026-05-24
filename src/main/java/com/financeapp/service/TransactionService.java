package com.financeapp.service;

import com.financeapp.dto.TransactionRequestDto;
import com.financeapp.dto.TransactionResponseDto;
import com.financeapp.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {
    TransactionResponseDto createTransaction(TransactionRequestDto requestDto, String userEmail);
    TransactionResponseDto updateTransaction(Long id, TransactionRequestDto requestDto, String userEmail);
    void deleteTransaction(Long id, String userEmail);
    Page<TransactionResponseDto> getTransactions(
            String userEmail,
            String category,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
}
