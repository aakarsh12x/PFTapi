package com.financeapp.service;

import com.financeapp.dto.AuthResponseDto;
import com.financeapp.dto.UserLoginDto;
import com.financeapp.dto.UserRegisterDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.entity.User;
import com.financeapp.exception.EmailAlreadyExistsException;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.mapper.UserMapper;
import com.financeapp.repository.UserRepository;
import com.financeapp.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDto register(UserRegisterDto registerDto) {
        String resolvedEmail = registerDto.getUsername() != null ? registerDto.getUsername() : registerDto.getEmail();
        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            throw new IllegalArgumentException("Username/Email is required");
        }

        if (userRepository.existsByEmail(resolvedEmail)) {
            throw new EmailAlreadyExistsException("Email '" + resolvedEmail + "' is already registered");
        }

        String resolvedFullName = registerDto.getFullName();
        String resolvedFirstName = registerDto.getFirstName();
        String resolvedLastName = registerDto.getLastName();

        if (resolvedFullName != null && !resolvedFullName.isBlank()) {
            if (resolvedFirstName == null || resolvedFirstName.isBlank()) {
                String[] parts = resolvedFullName.trim().split("\\s+", 2);
                resolvedFirstName = parts[0];
                resolvedLastName = parts.length > 1 ? parts[1] : "";
            }
        } else if (resolvedFirstName != null && resolvedLastName != null) {
            resolvedFullName = resolvedFirstName + " " + resolvedLastName;
        }

        User user = User.builder()
                .email(resolvedEmail)
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .firstName(resolvedFirstName)
                .lastName(resolvedLastName)
                .fullName(resolvedFullName)
                .phoneNumber(registerDto.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser);

        return AuthResponseDto.builder()
                .token(token)
                .user(userMapper.toResponseDto(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(UserLoginDto loginDto) {
        String resolvedEmail = loginDto.getUsername() != null ? loginDto.getUsername() : loginDto.getEmail();
        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            throw new IllegalArgumentException("Username/Email is required");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(resolvedEmail, loginDto.getPassword())
        );

        User user = userRepository.findByEmail(resolvedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + resolvedEmail));

        String token = jwtUtils.generateToken(user);

        return AuthResponseDto.builder()
                .token(token)
                .user(userMapper.toResponseDto(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponseDto(user);
    }
}
