package com.alejandro.expense_tracker.category;

import com.alejandro.expense_tracker.category.dto.CategoryRequest;
import com.alejandro.expense_tracker.category.dto.CategoryResponse;
import com.alejandro.expense_tracker.exceptions.CategoryAlreadyExistsException;
import com.alejandro.expense_tracker.exceptions.UserNotFoundException;
import com.alejandro.expense_tracker.user.User;
import com.alejandro.expense_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String name = request.name().strip();

        boolean alreadyExists = categoryRepository.existsByUser_IdAndNameIgnoreCaseAndType(
                userId, name, request.type());
        if (alreadyExists) {
            throw new CategoryAlreadyExistsException();
        }
        Category category = new Category(
                name,
                request.type(),
                user
        );
        Category categorySaved = categoryRepository.save(category);
        return toResponse(categorySaved);
    }

    @Transactional(readOnly = true)
    List<CategoryResponse> findAll(Long userId) {
        return categoryRepository.findAllByUser_IdOrderByNameAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType()
        );
    }

}
