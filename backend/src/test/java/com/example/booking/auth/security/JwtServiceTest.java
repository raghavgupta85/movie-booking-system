package com.example.booking.auth.security;

import com.example.booking.user.Role;
import com.example.booking.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-that-is-at-least-32-chars";

    private User user(Role role) {
        User u = new User();
        u.setEmail("raghav@test.com");
        u.setRole(role);
        return u;
    }

    @Test
    void tokenRoundTripKeepsEmailAndRole() {
        JwtService jwt = new JwtService(SECRET, 60_000L);

        Claims claims = jwt.parse(jwt.generateToken(user(Role.ADMIN)));

        assertEquals("raghav@test.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60_000L);
        String token = jwt.generateToken(user(Role.USER));
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("xx") ? "yy" : "xx");

        assertThrows(JwtException.class, () -> jwt.parse(tampered));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService issuer = new JwtService("a-completely-different-secret-key-of-32-chars!!", 60_000L);
        JwtService verifier = new JwtService(SECRET, 60_000L);

        String token = issuer.generateToken(user(Role.ADMIN));

        assertThrows(JwtException.class, () -> verifier.parse(token));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -1_000L);   // already expired when issued

        String token = jwt.generateToken(user(Role.USER));

        assertThrows(JwtException.class, () -> jwt.parse(token));
    }

    @Test
    void secretShorterThan32CharactersIsRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 60_000L));
    }
}
