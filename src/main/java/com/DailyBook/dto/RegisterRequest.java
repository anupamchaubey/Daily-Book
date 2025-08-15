package com.DailyBook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "how can I know you without your username ...")
    private String username;
    @NotBlank(message = "password is necessary na ...")
    private String password;

    @Email(message = "oops! invalid email ...")
    @NotBlank(message = "enter your email yaar...")
    private String email;
}
