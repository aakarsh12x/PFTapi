package com.financeapp.service;

import com.financeapp.dto.CategoryListResponseDto;
import com.financeapp.dto.CategoryRequestDto;
import com.financeapp.dto.CategoryResponseDto;
import com.financeapp.entity.Category;
import com.financeapp.entity.User;
import com.financeapp.exception.ConflictException;
import com.financeapp.exception.ForbiddenException;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.repository.CategoryRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public CategoryListResponseDto getAllCategories(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Category> categories = categoryRepository.findAllVisibleToUser(user);
        List<CategoryResponseDto> dtoList = categories.stream()
                .map(c -> CategoryResponseDto.builder()
                        .name(c.getName())
                        .type(c.getType())
                        .isCustom(c.isCustom())
                        .build())
                .collect(Collectors.toList());

        return CategoryListResponseDto.builder()
                .categories(dtoList)
                .build();
    }

    @Override
    @Transactional
    public CategoryResponseDto createCustomCategory(CategoryRequestDto requestDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        String categoryName = requestDto.getName().trim();
        
        // Check duplication (case-insensitive) in system categories or this user's custom categories
        boolean existsInSystem = categoryRepository.existsByNameIgnoreCaseAndUserIsNull(categoryName);
        boolean existsInUser = categoryRepository.existsByNameIgnoreCaseAndUser(categoryName, user);
        
        if (existsInSystem || existsInUser) {
            throw new ConflictException("Category '" + categoryName + "' already exists");
        }

        Category category = Category.builder()
                .name(categoryName)
                .type(requestDto.getType())
                .isCustom(true)
                .user(user)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponseDto.builder()
                .name(savedCategory.getName())
                .type(savedCategory.getType())
                .isCustom(savedCategory.isCustom())
                .build();
    }

    @Override
    @Transactional
    public void deleteCustomCategory(String name, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        String categoryName = name.trim();
        
        // Find visible category
        Category category = categoryRepository.findByNameVisibleToUser(categoryName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + categoryName));

        // Check if predefined system category
        if (!category.isCustom()) {
            throw new ForbiddenException("Cannot delete predefined system category: " + categoryName);
        }

        // Check if category belongs to another user
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to delete this category");
        }

        boolean isReferenced = transactionRepository.existsByUserAndCategoryIgnoreCase(user, categoryName);
        if (isReferenced) {
            throw new ConflictException("Categories currently referenced by active transactions cannot be deleted");
        }

        categoryRepository.delete(category);
    }
}
