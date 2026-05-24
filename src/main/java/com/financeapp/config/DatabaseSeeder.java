package com.financeapp.config;

import com.financeapp.entity.Category;
import com.financeapp.enums.TransactionType;
import com.financeapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // INCOME: Salary, Freelance, Dividend, Investments, Other Income
        List<String> incomes = List.of("Salary", "Freelance", "Dividend", "Investments", "Other Income");
        for (String inc : incomes) {
            if (!categoryRepository.existsByNameIgnoreCaseAndUserIsNull(inc)) {
                categoryRepository.save(Category.builder()
                        .name(inc)
                        .type(TransactionType.INCOME)
                        .isCustom(false)
                        .user(null)
                        .build());
            }
        }

        // EXPENSE: Food, Rent, Transportation, Entertainment, Healthcare, Utilities
        List<String> expenses = List.of("Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities");
        for (String exp : expenses) {
            if (!categoryRepository.existsByNameIgnoreCaseAndUserIsNull(exp)) {
                categoryRepository.save(Category.builder()
                        .name(exp)
                        .type(TransactionType.EXPENSE)
                        .isCustom(false)
                        .user(null)
                        .build());
            }
        }
        System.out.println("Default categories seeded successfully!");
    }
}
