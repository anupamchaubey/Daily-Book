package com.DailyBook.service;

import com.DailyBook.config.JwtTokenProvider;
import com.DailyBook.dto.AuthenticationResponse;
import com.DailyBook.dto.LoginRequest;
import com.DailyBook.dto.RegisterRequest;
import com.DailyBook.exception.UserAlreadyExistsException;
import com.DailyBook.model.User;
import com.DailyBook.model.UserProfile;
import com.DailyBook.repository.UserProfileRepository;
import com.DailyBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Save user once
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(Collections.singleton("USER"))
                .build();
        userRepository.save(user);

        // Create bare profile (using username as profile id)
        UserProfile profile = UserProfile.builder()
                .id(user.getUsername())
                .username(user.getUsername())
                .joinedAt(Instant.now())
                .build();
        userProfileRepository.save(profile);

        return "User registration done ...";
    }

    public AuthenticationResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // generate token
        String token = jwtTokenProvider.generateToken(authentication.getName());

        // compute numeric expiry in epoch millis using configured jwtExpiration
        long now = System.currentTimeMillis();
        long expiresAt = now + jwtTokenProvider.getJwtExpirationMillis();

        return new AuthenticationResponse(token, expiresAt);
    }
}
