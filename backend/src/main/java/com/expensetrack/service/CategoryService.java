package com.expensetrack.service;

import com.expensetrack.dto.CategoryDto;
import com.expensetrack.dto.CategoryRequest;
import com.expensetrack.entity.Category;
import com.expensetrack.entity.User;
import com.expensetrack.exception.ApiException;
import com.expensetrack.exception.ResourceNotFoundException;
import com.expensetrack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public List<CategoryDto> getUserCategories() {
        User user = userService.getAuthenticatedUser();
        return categoryRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id) {
        User user = userService.getAuthenticatedUser();
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryRequest request) {
        User user = userService.getAuthenticatedUser();
        String name = request.getName().trim();

        if (categoryRepository.existsByNameAndUser(name, user)) {
            throw new ApiException("Category with name '" + name + "' already exists");
        }

        Category category = Category.builder()
                .name(name)
                .isDefault(false)
                .user(user)
                .build();

        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryRequest request) {
        User user = userService.getAuthenticatedUser();
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        String name = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameAndUser(name, user)) {
            throw new ApiException("Category with name '" + name + "' already exists");
        }

        category.setName(name);
        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = userService.getAuthenticatedUser();
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        categoryRepository.delete(category);
    }

    public CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .isDefault(category.isDefault())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
