package com.financeapp.controller;

import com.financeapp.dto.AuthResponseDto;
import com.financeapp.dto.UserLoginDto;
import com.financeapp.dto.UserRegisterDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and session context")
public class AuthController {

    private final AuthService authService;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and returns success message with userId.")
    @ApiResponse(responseCode = "201", description = "User successfully registered")
    @ApiResponse(responseCode = "400", description = "Validation error on registration fields")
    @ApiResponse(responseCode = "409", description = "Email/Username already exists in the system")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterDto registerDto) {
        AuthResponseDto response = authService.register(registerDto);
        
        Map<String, Object> body = new HashMap<>();
        body.put("message", "User registered successfully");
        body.put("userId", response.getUser().getId());
        // Return token and user info for frontend backwards compatibility
        body.put("token", response.getToken());
        body.put("user", response.getUser());
        
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates user credentials and returns successful message / sets session cookie.")
    @ApiResponse(responseCode = "200", description = "User successfully authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody UserLoginDto loginDto,
            HttpServletRequest request
    ) {
        AuthResponseDto response = authService.login(loginDto);
        
        // Manual SecurityContext session registration for Cookie-based Auth
        String resolvedEmail = loginDto.getUsername() != null ? loginDto.getUsername() : loginDto.getEmail();
        UserDetails userDetails = userDetailsService.loadUserByUsername(resolvedEmail);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Login successful");
        // Return token and user info for frontend backwards compatibility
        body.put("token", response.getToken());
        body.put("user", response.getUser());
        
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Logs out user by invalidating the HTTP session.")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        Map<String, String> body = new HashMap<>();
        body.put("message", "Logout successful");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Retrieves profile information for the currently logged in user context.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto currentUser = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(currentUser);
    }
}
