package com.mtrxxp.backend.config;

import com.mtrxxp.backend.room.Room;
import com.mtrxxp.backend.room.RoomRepository;
import com.mtrxxp.backend.room.RoomStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Populates the rooms table with demo data on startup,
 * only if it is empty. Repeated runs do not duplicate anything.
 */
@Component
public class RoomDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoomDataInitializer.class);

    private final RoomRepository roomRepository;

    public RoomDataInitializer(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() > 0) {
            return;
        }

        List<Room> rooms = List.of(
                room("Aurora", "Floor 1, Wing A", 6, "150.00",
                        "Small meeting room with a TV panel", RoomStatus.AVAILABLE),
                room("Borealis", "Floor 2, Wing B", 12, "300.00",
                        "Meeting room with a projector and video conferencing", RoomStatus.AVAILABLE),
                room("Cosmos", "Floor 3", 30, "700.00",
                        "Conference hall for presentations and webinars", RoomStatus.AVAILABLE),
                room("Delta", "Floor 1, Wing C", 4, "100.00",
                        "Room for quick 1-on-1 meetings", RoomStatus.AVAILABLE),
                room("Everest", "Floor 4", 50, "1200.00",
                        "Large hall for corporate events", RoomStatus.MAINTENANCE)
        );

        roomRepository.saveAll(rooms);
        log.info("Initialized demo rooms: {}", rooms.size());
    }

    private Room room(String name, String location, int capacity,
                      String pricePerHour, String description, RoomStatus status) {
        Room room = new Room();
        room.setName(name);
        room.setLocation(location);
        room.setCapacity(capacity);
        room.setPricePerHour(new BigDecimal(pricePerHour));
        room.setDescription(description);
        room.setRoomStatus(status);
        return room;
    }
}
