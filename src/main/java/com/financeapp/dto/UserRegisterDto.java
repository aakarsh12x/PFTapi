package com.financeapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    @NotBlank(message = "Username/email is required")
    private String username;

    // Optional - kept for API compatibility
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;

    public void setEmail(String email) {
        this.email = email;
        if (this.username == null || this.username.isBlank()) {
            this.username = email;
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (this.email == null || this.email.isBlank()) {
            this.email = username;
        }
    }
}

