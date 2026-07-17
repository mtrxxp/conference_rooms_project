package com.mtrxxp.backend.room;

import com.mtrxxp.backend.room.dto.RoomRequest;
import com.mtrxxp.backend.room.dto.RoomResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private RoomRequest request() {
        return new RoomRequest("Aurora", "Floor 2", 12,
                new BigDecimal("100.00"), "meeting room", RoomStatus.AVAILABLE);
    }

    @Test
    void create_success() {
        when(roomRepository.existsByName("Aurora")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomResponse response = roomService.create(request());

        assertThat(response.name()).isEqualTo("Aurora");
        assertThat(response.capacity()).isEqualTo(12);
        assertThat(response.roomStatus()).isEqualTo(RoomStatus.AVAILABLE);
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(roomRepository.existsByName("Aurora")).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(request()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void update_keepingSameName_doesNotCheckDuplicate() {
        Room existing = new Room();
        existing.setId(1L);
        existing.setName("Aurora");
        existing.setPricePerHour(new BigDecimal("50.00"));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomResponse response = roomService.update(1L, request());

        assertThat(response.pricePerHour()).isEqualByComparingTo("100.00");
        verify(roomRepository, never()).existsByName(any());
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(roomRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> roomService.delete(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(roomRepository, never()).deleteById(any());
    }
}
