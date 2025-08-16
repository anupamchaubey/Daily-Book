package com.DailyBook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "I need username...")
    private String username;

    @NotBlank(message = "please enter your password boss...")
    private String password;
}
