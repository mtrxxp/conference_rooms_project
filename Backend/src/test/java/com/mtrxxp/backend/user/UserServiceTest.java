package com.mtrxxp.backend.user;

import com.mtrxxp.backend.user.dto.UpdateUserRequest;
import com.mtrxxp.backend.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Ivan");
        user.setLastName("Petrov");
        user.setEmail("ivan@example.com");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void update_success_changesFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(1L,
                new UpdateUserRequest("Peter", "Ivanov", "+79990000000"));

        assertThat(response.firstName()).isEqualTo("Peter");
        assertThat(response.lastName()).isEqualTo("Ivanov");
        assertThat(response.phoneNumber()).isEqualTo("+79990000000");
    }

    @Test
    void getByEmail_notFound_throwsNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(userRepository, never()).deleteById(any());
    }
}
