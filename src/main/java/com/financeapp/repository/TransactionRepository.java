package com.financeapp.repository;

import com.financeapp.entity.User;
import com.financeapp.entity.Transaction;
import com.financeapp.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND " +
           "(:category IS NULL OR LOWER(t.category) = :category) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "t.date >= :startDate AND t.date <= :endDate")
    Page<Transaction> findFiltered(
        @Param("user") User user,
        @Param("category") String category,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal sumAmountByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateRange(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t.category as category, SUM(t.amount) as amount FROM Transaction t WHERE t.user = :user AND t.type = :type GROUP BY t.category")
    List<CategorySumProjection> getCategoryBreakdown(@Param("user") User user, @Param("type") TransactionType type);

    boolean existsByUserAndCategoryIgnoreCase(User user, String category);

    List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
           "FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    BigDecimal calculateSavingsSince(@Param("user") User user, @Param("startDate") LocalDate startDate);

    interface CategorySumProjection {
        String getCategory();
        BigDecimal getAmount();
    }
}
