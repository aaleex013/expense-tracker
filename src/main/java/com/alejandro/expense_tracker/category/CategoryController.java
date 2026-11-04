package com.alejandro.expense_tracker.category;


import com.alejandro.expense_tracker.category.dto.CategoryRequest;
import com.alejandro.expense_tracker.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final  CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CategoryRequest request) {
        Long userId = getUserId(jwt);
        return categoryService.create(userId, request);
    }

    @GetMapping
    public List<CategoryResponse> findAll(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = getUserId(jwt);

        return categoryService.findAll(userId);
    }

    private Long getUserId(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no contiene el identificador del usuario"
            );
        }

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El identificador del token no es válido"
            );
        }
    }
}
