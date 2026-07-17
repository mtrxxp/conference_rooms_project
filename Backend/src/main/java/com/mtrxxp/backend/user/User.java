package com.mtrxxp.backend.user;

import com.mtrxxp.backend.booking.Booking;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String firstName;
    String lastName;
    @Column(unique = true, nullable = false)
    String email;
    String password;
    String phoneNumber;
    @Enumerated(EnumType.STRING)
    Role role = Role.USER;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Booking> bookings = new ArrayList<>();
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdDate = now;
        modifiedDate = now;

        if (role == null) {
            role = Role.USER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
