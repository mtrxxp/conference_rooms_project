package com.mtrxxp.backend.security;

import com.mtrxxp.backend.user.Role;
import com.mtrxxp.backend.user.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-1234567890";

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("ivan@example.com");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void generateThenValidate_returnsSubject() {
        JwtService jwtService = new JwtService(SECRET, 86_400_000L);

        String token = jwtService.generateToken(user());
        Optional<String> subject = jwtService.validateAndGetSubject(token);

        assertThat(subject).contains("ivan@example.com");
    }

    @Test
    void tamperedToken_isRejected() {
        JwtService jwtService = new JwtService(SECRET, 86_400_000L);
        String token = jwtService.generateToken(user());

        // corrupt the signature - change the last character
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");

        assertThat(jwtService.validateAndGetSubject(tampered)).isEmpty();
    }

    @Test
    void tokenSignedWithDifferentSecret_isRejected() {
        String token = new JwtService(SECRET, 86_400_000L).generateToken(user());
        JwtService other = new JwtService("completely-different-secret-key-0987654321", 86_400_000L);

        assertThat(other.validateAndGetSubject(token)).isEmpty();
    }

    @Test
    void expiredToken_isRejected() {
        // negative lifetime => exp is in the past
        JwtService jwtService = new JwtService(SECRET, -10_000L);
        String token = jwtService.generateToken(user());

        assertThat(jwtService.validateAndGetSubject(token)).isEmpty();
    }

    @Test
    void malformedToken_isRejected() {
        JwtService jwtService = new JwtService(SECRET, 86_400_000L);

        assertThat(jwtService.validateAndGetSubject("not-a-jwt")).isEmpty();
        assertThat(jwtService.validateAndGetSubject("")).isEmpty();
        assertThat(jwtService.validateAndGetSubject(null)).isEmpty();
    }
}
