package com.mtrxxp.backend.booking;

import com.mtrxxp.backend.booking.dto.BookingRequest;
import com.mtrxxp.backend.booking.dto.BookingResponse;
import com.mtrxxp.backend.booking.dto.BookingStatusUpdateRequest;
import com.mtrxxp.backend.room.Room;
import com.mtrxxp.backend.room.RoomRepository;
import com.mtrxxp.backend.room.RoomStatus;
import com.mtrxxp.backend.user.User;
import com.mtrxxp.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingService {

    /** Statuses in which a booking occupies the room's time slot. */
    private static final Set<BookingStatus> ACTIVE_STATUSES =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll().stream()
                .map(BookingService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String email) {
        return bookingRepository.findByUserEmailOrderByStartDateDesc(email).stream()
                .map(BookingService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(findBooking(id));
    }

    @Transactional
    public BookingResponse create(BookingRequest request, String email) {
        User user = findUser(email);
        Room room = findRoom(request.roomId());

        validateInterval(request.startDate(), request.endDate());
        validateRoomAvailable(room);
        validateNoOverlap(room.getId(), request.startDate(), request.endDate(), null);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setNote(request.note());
        booking.setPrice(calculatePrice(room, request.startDate(), request.endDate()));
        booking.setBookingStatus(BookingStatus.PENDING);

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse update(Long id, BookingRequest request, String email) {
        Booking booking = findBooking(id);
        requireOwner(booking, email);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only bookings in PENDING status can be modified");
        }

        Room room = findRoom(request.roomId());
        validateInterval(request.startDate(), request.endDate());
        validateRoomAvailable(room);
        validateNoOverlap(room.getId(), request.startDate(), request.endDate(), booking.getId());

        booking.setRoom(room);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setNote(request.note());
        booking.setPrice(calculatePrice(room, request.startDate(), request.endDate()));

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatusUpdateRequest request) {
        Booking booking = findBooking(id);
        booking.setBookingStatus(request.bookingStatus());
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public void cancel(Long id, String email) {
        Booking booking = findBooking(id);
        requireOwner(booking, email);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED
                || booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking is already completed or cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // --- helper methods ---

    private void validateInterval(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "End date must be after start date");
        }
    }

    private void validateRoomAvailable(Room room) {
        if (room.getRoomStatus() != RoomStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Room is not available for booking (status: " + room.getRoomStatus() + ")");
        }
    }

    private void validateNoOverlap(Long roomId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (bookingRepository.existsOverlapping(roomId, start, end, ACTIVE_STATUSES, excludeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Room is already booked for the selected time interval");
        }
    }

    private void requireOwner(Booking booking, String email) {
        if (booking.getUser() == null || !booking.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No access to this booking");
        }
    }

    private BigDecimal calculatePrice(Room room, LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return room.getPricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found: " + id));
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Room not found: " + id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + email));
    }

    private static BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser() != null ? booking.getUser().getId() : null,
                booking.getUser() != null ? booking.getUser().getEmail() : null,
                booking.getRoom() != null ? booking.getRoom().getId() : null,
                booking.getRoom() != null ? booking.getRoom().getName() : null,
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getPrice(),
                booking.getBookingStatus(),
                booking.getNote(),
                booking.getCreatedDate()
        );
    }
}
