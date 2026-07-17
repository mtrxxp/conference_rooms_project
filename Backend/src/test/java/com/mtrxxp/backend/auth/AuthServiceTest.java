package com.mtrxxp.backend.auth;

import com.mtrxxp.backend.auth.dto.AuthResponse;
import com.mtrxxp.backend.auth.dto.LoginRequest;
import com.mtrxxp.backend.auth.dto.RegisterRequest;
import com.mtrxxp.backend.security.JwtService;
import com.mtrxxp.backend.user.Role;
import com.mtrxxp.backend.user.User;
import com.mtrxxp.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest() {
        return new RegisterRequest("Ivan", "Petrov", "ivan@example.com", "secret123", "+79990000000");
    }

    @Test
    void register_success_encodesPasswordAndIssuesToken() {
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest());

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("ivan@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);

        // password is stored only in hashed form
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void register_emailExists_throwsConflict() {
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("ivan@example.com");
        user.setPassword("hashed");
        user.setRole(Role.ADMIN);
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("ivan@example.com", "secret123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = new User();
        user.setEmail("ivan@example.com");
        user.setPassword("hashed");
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ivan@example.com", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "secret123")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
