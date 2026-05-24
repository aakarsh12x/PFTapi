package com.financeapp.repository;

import com.financeapp.entity.Category;
import com.financeapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user")
    List<Category> findAllVisibleToUser(@Param("user") User user);
    
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user = :user) AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByNameVisibleToUser(@Param("name") String name, @Param("user") User user);
    
    boolean existsByNameIgnoreCaseAndUser(String name, User user);
    
    boolean existsByNameIgnoreCaseAndUserIsNull(String name);
    
    Optional<Category> findByNameIgnoreCaseAndUser(String name, User user);
}
