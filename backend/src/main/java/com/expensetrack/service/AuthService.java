package com.expensetrack.service;

import com.expensetrack.dto.AuthResponse;
import com.expensetrack.dto.LoginRequest;
import com.expensetrack.dto.RegisterRequest;
import com.expensetrack.dto.UserDto;
import com.expensetrack.entity.Budget;
import com.expensetrack.entity.Category;
import com.expensetrack.entity.User;
import com.expensetrack.exception.ApiException;
import com.expensetrack.repository.BudgetRepository;
import com.expensetrack.repository.CategoryRepository;
import com.expensetrack.repository.UserRepository;
import com.expensetrack.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "D-Mart",
            "Chicken",
            "Eggs",
            "Vegetables",
            "Rapido",
            "Church Offerings",
            "Milk & Curd"
    );

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email is already registered: " + request.getEmail());
        }

        // Hashing password securely with BCrypt
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        // Seed default categories for this specific user
        for (String catName : DEFAULT_CATEGORIES) {
            Category category = Category.builder()
                    .name(catName)
                    .isDefault(true)
                    .user(user)
                    .build();
            categoryRepository.save(category);
        }

        // Initialize default budget container
        Budget budget = Budget.builder()
                .user(user)
                .dailyBudget(new BigDecimal("500.00"))
                .monthlyBudget(new BigDecimal("15000.00"))
                .build();
        budgetRepository.save(budget);

        String token = jwtUtils.generateToken(user.getEmail());

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ApiException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .message("Login successful")
                .build();
    }

    public UserDto getCurrentUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
