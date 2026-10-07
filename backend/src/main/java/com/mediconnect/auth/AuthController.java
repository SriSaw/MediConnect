package com.mediconnect.auth;

import com.mediconnect.auth.dto.AuthResponse;
import com.mediconnect.auth.dto.LoginRequest;
import com.mediconnect.auth.dto.RefreshTokenRequest;
import com.mediconnect.auth.dto.RegisterRequest;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Registration, login, and token refresh endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new patient or healthcare professional")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user information")
    public ResponseEntity<UserResponse> getMe(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(authService.getCurrentUser(currentUser));
    }
}
