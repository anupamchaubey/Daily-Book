package com.DailyBook.service;

import com.DailyBook.config.JwtTokenProvider;
import com.DailyBook.dto.LoginRequest;
import com.DailyBook.dto.RegisterRequest;
import com.DailyBook.model.User;
import com.DailyBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

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
    public String login(LoginRequest request){
        System.out.println("🔑 AuthService.login called for: " + request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            System.out.println("✅ Authentication successful for: " + request.getUsername());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(request.getUsername());
            System.out.println("🎟️ Generated JWT: " + token);
            return token;
        } catch (Exception e) {
            System.out.println("❌ Authentication failed: " + e.getMessage());
            throw e; // let Spring handle and return 403
        }
    }


}
