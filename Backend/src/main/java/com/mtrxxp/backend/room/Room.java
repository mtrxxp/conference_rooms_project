package com.mtrxxp.backend.room;

import com.mtrxxp.backend.booking.Booking;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true, nullable = false)
    String name;
    String location;
    Integer capacity;
    BigDecimal pricePerHour;
    String description;
    @Enumerated(EnumType.STRING)
    RoomStatus roomStatus = RoomStatus.AVAILABLE;
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Booking> bookings = new ArrayList<>();
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdDate = now;
        modifiedDate = now;

        if (roomStatus == null) {
            roomStatus = RoomStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
