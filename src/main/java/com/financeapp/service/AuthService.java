package com.financeapp.service;

import com.financeapp.dto.AuthResponseDto;
import com.financeapp.dto.UserLoginDto;
import com.financeapp.dto.UserRegisterDto;
import com.financeapp.dto.UserResponseDto;

public interface AuthService {
    AuthResponseDto register(UserRegisterDto registerDto);
    AuthResponseDto login(UserLoginDto loginDto);
    UserResponseDto getCurrentUser(String email);
}
