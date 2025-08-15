package com.DailyBook.service;

import com.DailyBook.dto.RegisterRequest;
import com.DailyBook.model.User;
import com.DailyBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("email already exists");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("username already exists");
        }

        User user= User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(Collections.singleton("USER"))
                .build();
        userRepository.save(user);
        return "User registration done ...";
    }
}
