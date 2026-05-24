package com.financeapp.controller;

import com.financeapp.dto.CategoryListResponseDto;
import com.financeapp.dto.CategoryRequestDto;
import com.financeapp.dto.CategoryResponseDto;
import com.financeapp.service.CategoryService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Endpoints for managing transaction categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all predefined and custom categories visible to the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories list")
    public ResponseEntity<CategoryListResponseDto> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CategoryListResponseDto response = categoryService.getAllCategories(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create custom category", description = "Creates a new custom category for the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Category successfully created")
    @ApiResponse(responseCode = "400", description = "Validation error on request body")
    @ApiResponse(responseCode = "409", description = "Category name already exists")
    public ResponseEntity<CategoryResponseDto> createCustomCategory(
            @Valid @RequestBody CategoryRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CategoryResponseDto response = categoryService.createCustomCategory(requestDto, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Delete custom category", description = "Deletes a custom category owned by the user by name.")
    @ApiResponse(responseCode = "200", description = "Category successfully deleted")
    @ApiResponse(responseCode = "400", description = "Category is in use or cannot be deleted")
    @ApiResponse(responseCode = "403", description = "Cannot delete predefined system categories")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Map<String, String>> deleteCustomCategory(
            @PathVariable String name,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        categoryService.deleteCustomCategory(name, userDetails.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        return ResponseEntity.ok(response);
    }
}
