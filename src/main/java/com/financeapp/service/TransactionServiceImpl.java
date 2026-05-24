package com.financeapp.service;

import com.financeapp.dto.TransactionRequestDto;
import com.financeapp.dto.TransactionResponseDto;
import com.financeapp.entity.Transaction;
import com.financeapp.entity.User;
import com.financeapp.entity.Category;
import com.financeapp.enums.TransactionType;
import com.financeapp.exception.ForbiddenException;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.mapper.TransactionMapper;
import com.financeapp.repository.UserRepository;
import com.financeapp.repository.TransactionRepository;
import com.financeapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto requestDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        LocalDate resolvedDate = requestDto.getDate() != null ? requestDto.getDate() : requestDto.getTransactionDate();
        if (resolvedDate == null) {
            resolvedDate = LocalDate.now();
        }
        if (resolvedDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be a future date");
        }

        String categoryName = requestDto.getCategory();
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }

        // Validate that category is visible to this user
        Category category = categoryRepository.findByNameVisibleToUser(categoryName, user)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + categoryName));

        Transaction transaction = transactionMapper.toEntity(requestDto);
        transaction.setUser(user);
        transaction.setDate(resolvedDate);
        // Derive type from category
        transaction.setType(category.getType());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto updateTransaction(Long id, TransactionRequestDto requestDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to update this transaction");
        }

        if (requestDto.getAmount() != null) {
            if (requestDto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be a positive number");
            }
            transaction.setAmount(requestDto.getAmount());
        }

        if (requestDto.getCategory() != null && !requestDto.getCategory().isBlank()) {
            Category category = categoryRepository.findByNameVisibleToUser(requestDto.getCategory(), user)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + requestDto.getCategory()));
            transaction.setCategory(category.getName());
            transaction.setType(category.getType());
        }

        if (requestDto.getDescription() != null) {
            transaction.setDescription(requestDto.getDescription());
        }

        if (requestDto.getTitle() != null) {
            transaction.setTitle(requestDto.getTitle());
        } else if (requestDto.getCategory() != null) {
            transaction.setTitle(requestDto.getCategory());
        }

        // Date is not updated as per the spec requirements ("modify any transaction field except the date field")

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toResponseDto(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to delete this transaction");
        }

        transactionRepository.delete(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getTransactions(
            String userEmail,
            String category,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        String queryCategory = (category != null && !category.trim().isEmpty()) ? category.trim().toLowerCase() : null;

        Page<Transaction> transactions = transactionRepository.findFiltered(
                user, queryCategory, type, startDate, endDate, pageable
        );

        return transactions.map(transactionMapper::toResponseDto);
    }
}
