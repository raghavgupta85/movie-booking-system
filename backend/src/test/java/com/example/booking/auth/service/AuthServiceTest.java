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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerStoresAHashNotThePasswordAndForcesRoleUser() {
        when(userRepository.existsByEmail("raghav@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = service.register(new RegisterRequest("Raghav", "  Raghav@Test.COM ", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("HASH", saved.getPasswordHash());
        assertEquals("raghav@test.com", saved.getEmail());   // trimmed and lower-cased
        assertEquals(Role.USER, saved.getRole());
        assertEquals("USER", response.role());
    }

    @Test
    void registerRejectsDuplicateEmailBeforeHashing() {
        when(userRepository.existsByEmail("raghav@test.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> service.register(new RegisterRequest("Raghav", "raghav@test.com", "password123")));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);   // BCrypt is slow, so don't waste it
    }

    @Test
    void loginWithWrongPasswordFailsAndIssuesNoToken() {
        User user = new User();
        user.setEmail("raghav@test.com");
        user.setPasswordHash("HASH");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("raghav@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "HASH")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("raghav@test.com", "wrongpass")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void loginWithUnknownEmailThrowsTheSameException() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("nobody@test.com", "whatever1")));

        assertEquals("Invalid email or password", ex.getMessage());   // same message as a wrong password
    }

    @Test
    void loginSuccessReturnsTheToken() {
        User user = new User();
        user.setEmail("raghav@test.com");
        user.setPasswordHash("HASH");
        user.setRole(Role.ADMIN);
        when(userRepository.findByEmail("raghav@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "HASH")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("TOKEN");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse response = service.login(new LoginRequest("RAGHAV@test.com", "password123"));

        assertEquals("TOKEN", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("ADMIN", response.role());
    }
}
