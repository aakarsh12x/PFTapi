package com.financeapp.service;

import com.financeapp.dto.AuthResponseDto;
import com.financeapp.dto.UserRegisterDto;
import com.financeapp.dto.UserResponseDto;
import com.financeapp.entity.User;
import com.financeapp.exception.EmailAlreadyExistsException;
import com.financeapp.mapper.UserMapper;
import com.financeapp.repository.UserRepository;
import com.financeapp.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegisterDto registerDto;
    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        registerDto = UserRegisterDto.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashed_password")
                .firstName("John")
                .lastName("Doe")
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void register_ShouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtils.generateToken(any(User.class))).thenReturn("mock_token");
        when(userMapper.toResponseDto(any(User.class))).thenReturn(userResponseDto);

        AuthResponseDto result = authService.register(registerDto);

        assertNotNull(result);
        assertEquals("mock_token", result.getToken());
        assertEquals("test@example.com", result.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerDto));
        verify(userRepository, never()).save(any(User.class));
    }
}
