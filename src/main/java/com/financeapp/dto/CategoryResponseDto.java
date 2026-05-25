package com.financeapp.dto;

import com.financeapp.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDto {
    private String name;
    private TransactionType type;
    
    @JsonProperty("isCustom")
    private boolean isCustom;

    @JsonProperty("custom")
    public boolean getCustom() {
        return isCustom;
    }
}
