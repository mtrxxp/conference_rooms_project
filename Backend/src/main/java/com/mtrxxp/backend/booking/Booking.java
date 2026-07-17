package com.mtrxxp.backend.booking;

import com.mtrxxp.backend.room.Room;
import com.mtrxxp.backend.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    User user;
    @ManyToOne(fetch = FetchType.LAZY)
    Room room;
    LocalDateTime startDate;
    LocalDateTime endDate;
    BigDecimal price;
    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus = BookingStatus.PENDING;
    String note;
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdDate = now;
        modifiedDate = now;

        if (bookingStatus == null) {
            bookingStatus = BookingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
