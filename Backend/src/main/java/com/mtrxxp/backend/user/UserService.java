package com.mtrxxp.backend.user;

import com.mtrxxp.backend.user.dto.UpdateRoleRequest;
import com.mtrxxp.backend.user.dto.UpdateUserRequest;
import com.mtrxxp.backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return toResponse(findByEmail(email));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findById(id);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = findById(id);
        user.setRole(request.role());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + email));
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedDate()
        );
    }
}
