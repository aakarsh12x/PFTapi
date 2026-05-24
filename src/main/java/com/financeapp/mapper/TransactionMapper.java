package com.financeapp.mapper;

import com.financeapp.dto.TransactionRequestDto;
import com.financeapp.dto.TransactionResponseDto;
import com.financeapp.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        java.time.LocalDate resolvedDate = dto.getDate() != null ? dto.getDate() : dto.getTransactionDate();
        String resolvedTitle = dto.getTitle() != null ? dto.getTitle() : dto.getCategory();

        return Transaction.builder()
                .title(resolvedTitle)
                .amount(dto.getAmount())
                .type(dto.getType())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .date(resolvedDate)
                .build();
    }

    public TransactionResponseDto toResponseDto(Transaction entity) {
        if (entity == null) {
            return null;
        }
        return TransactionResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .amount(entity.getAmount())
                .type(entity.getType())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .date(entity.getDate())
                .transactionDate(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
