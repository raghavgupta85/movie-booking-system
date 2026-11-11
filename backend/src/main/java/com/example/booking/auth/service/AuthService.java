package com.example.booking.auth.service;

import com.example.booking.auth.dto.AuthResponse;
import com.example.booking.auth.dto.LoginRequest;
import com.example.booking.auth.dto.RegisterRequest;
import com.example.booking.auth.dto.UserResponse;
import com.example.booking.auth.security.JwtService;
import com.example.booking.exception.DuplicateEmailException;
import com.example.booking.exception.InvalidCredentialsException;
import com.example.booking.user.Role;
import com.example.booking.user.User;
import com.example.booking.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already registered");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(Role.USER);   // never taken from the request
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // two simultaneous registrations: the unique index on email wins
            throw new DuplicateEmailException("Email already registered");
        }
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                jwtService.getExpirationMs(), user.getEmail(), user.getRole().name());
    }
}
