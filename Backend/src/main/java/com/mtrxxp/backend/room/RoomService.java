package com.mtrxxp.backend.room;

import com.mtrxxp.backend.room.dto.RoomRequest;
import com.mtrxxp.backend.room.dto.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<RoomResponse> getAll() {
        return roomRepository.findAll().stream()
                .map(RoomService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        return toResponse(findRoom(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        if (roomRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Room with name '" + request.name() + "' already exists");
        }

        Room room = new Room();
        room.setName(request.name());
        room.setLocation(request.location());
        room.setCapacity(request.capacity());
        room.setPricePerHour(request.pricePerHour());
        room.setDescription(request.description());
        if (request.roomStatus() != null) {
            room.setRoomStatus(request.roomStatus());
        }

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = findRoom(id);

        if (!room.getName().equals(request.name()) && roomRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Room with name '" + request.name() + "' already exists");
        }

        room.setName(request.name());
        room.setLocation(request.location());
        room.setCapacity(request.capacity());
        room.setPricePerHour(request.pricePerHour());
        room.setDescription(request.description());
        if (request.roomStatus() != null) {
            room.setRoomStatus(request.roomStatus());
        }

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + id);
        }
        roomRepository.deleteById(id);
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Room not found: " + id));
    }

    private static RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getLocation(),
                room.getCapacity(),
                room.getPricePerHour(),
                room.getDescription(),
                room.getRoomStatus(),
                room.getCreatedDate()
        );
    }
}
