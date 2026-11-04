package com.alejandro.expense_tracker.category;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;


public interface CategoryRepository  extends JpaRepository<Category, Long> {
    List<Category> findAllByUser_IdOrderByNameAsc(Long userId);
    Optional<Category> findByIdAndUser_Id(Long id, Long userId);
    boolean existsByUser_IdAndNameIgnoreCaseAndType(Long userId, String name, CategoryType type);
}
