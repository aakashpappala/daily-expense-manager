package com.expensetrack.controller;

import com.expensetrack.dto.AuthResponse;
import com.expensetrack.dto.LoginRequest;
import com.expensetrack.dto.RegisterRequest;
import com.expensetrack.dto.UserDto;
import com.expensetrack.entity.User;
import com.expensetrack.service.AuthService;
import com.expensetrack.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(authService.getCurrentUserDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Client side removes JWT token
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
