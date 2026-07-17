package com.mtrxxp.backend.booking;

import com.mtrxxp.backend.booking.dto.BookingRequest;
import com.mtrxxp.backend.booking.dto.BookingResponse;
import com.mtrxxp.backend.booking.dto.BookingStatusUpdateRequest;
import com.mtrxxp.backend.room.Room;
import com.mtrxxp.backend.room.RoomRepository;
import com.mtrxxp.backend.room.RoomStatus;
import com.mtrxxp.backend.user.User;
import com.mtrxxp.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private static final String EMAIL = "user@example.com";
    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = start.plusHours(2);

    private User user() {
        User user = new User();
        user.setId(10L);
        user.setEmail(EMAIL);
        return user;
    }

    private Room room(RoomStatus status) {
        Room room = new Room();
        room.setId(5L);
        room.setName("Aurora");
        room.setPricePerHour(new BigDecimal("100.00"));
        room.setRoomStatus(status);
        return room;
    }

    private BookingRequest request() {
        return new BookingRequest(5L, start, end, "team meeting");
    }

    @Test
    void create_success_computesPriceAndSaves() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room(RoomStatus.AVAILABLE)));
        when(bookingRepository.existsOverlapping(eq(5L), any(), any(), any(), isNull())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.create(request(), EMAIL);

        assertThat(response.roomId()).isEqualTo(5L);
        assertThat(response.userEmail()).isEqualTo(EMAIL);
        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.PENDING);
        // 2 hours x 100.00 = 200.00
        assertThat(response.price()).isEqualByComparingTo("200.00");
    }

    @Test
    void create_roomNotAvailable_throwsConflict() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room(RoomStatus.MAINTENANCE)));

        assertThatThrownBy(() -> bookingService.create(request(), EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_overlappingInterval_throwsConflict() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room(RoomStatus.AVAILABLE)));
        when(bookingRepository.existsOverlapping(eq(5L), any(), any(),
                any(Collection.class), isNull())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(request(), EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void create_endBeforeStart_throwsBadRequest() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room(RoomStatus.AVAILABLE)));
        BookingRequest bad = new BookingRequest(5L, end, start, null);

        assertThatThrownBy(() -> bookingService.create(bad, EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request(), EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void cancel_notOwner_throwsForbidden() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user());
        booking.setBookingStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(1L, "someone-else@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void cancel_owner_setsCancelledStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user());
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        bookingService.cancel(1L, EMAIL);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void update_notPending_throwsConflict() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user());
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.update(1L, request(), EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void updateStatus_setsNewStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user());
        booking.setBookingStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.updateStatus(1L,
                new BookingStatusUpdateRequest(BookingStatus.CONFIRMED));

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
