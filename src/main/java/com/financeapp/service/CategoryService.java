package com.financeapp.service;

import com.financeapp.dto.CategoryListResponseDto;
import com.financeapp.dto.CategoryRequestDto;
import com.financeapp.dto.CategoryResponseDto;

public interface CategoryService {
    CategoryListResponseDto getAllCategories(String userEmail);
    CategoryResponseDto createCustomCategory(CategoryRequestDto requestDto, String userEmail);
    void deleteCustomCategory(String name, String userEmail);
}
