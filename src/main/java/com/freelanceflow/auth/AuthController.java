package com.freelanceflow.auth;

import com.freelanceflow.user.dto.AuthResponse;
import com.freelanceflow.user.dto.LoginRequest;
import com.freelanceflow.user.dto.RegisterRequest;
import com.freelanceflow.user.dto.RefreshRequest;
import com.freelanceflow.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new freelancer", description = "Creates a new user account. Returns user details without tokens.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully registered")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login to the system", description = "Authenticates user and returns Access and Refresh JWTs")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Invalid credentials", content = @Content)
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh an Access Token", description = "Exchanges a valid refresh token for a new temporary access token")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tokens refreshed successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Refresh token expired or invalid", content = @Content)
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
