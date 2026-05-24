package com.financeapp.dto;

import com.financeapp.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDto {
    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type (INCOME or EXPENSE) is required")
    private TransactionType type;
}
