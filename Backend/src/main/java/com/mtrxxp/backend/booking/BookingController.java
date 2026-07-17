package com.mtrxxp.backend.booking;

import com.mtrxxp.backend.booking.dto.BookingRequest;
import com.mtrxxp.backend.booking.dto.BookingResponse;
import com.mtrxxp.backend.booking.dto.BookingStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BookingResponse>> getAll() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMy(Principal principal) {
        return ResponseEntity.ok(bookingService.getMyBookings(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request,
                                                  Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(request, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody BookingRequest request,
                                                  Principal principal) {
        return ResponseEntity.ok(bookingService.update(id, request, principal.getName()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BookingResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody BookingStatusUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Principal principal) {
        bookingService.cancel(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
