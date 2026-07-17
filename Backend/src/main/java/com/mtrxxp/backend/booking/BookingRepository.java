package com.mtrxxp.backend.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserEmailOrderByStartDateDesc(String email);

    /**
     * Checks whether the room has a time-overlapping booking in one of the
     * active statuses. Intervals overlap when start < existingEnd AND
     * end > existingStart. The excludeId parameter allows excluding the current
     * booking on update (pass null on create).
     */
    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :roomId
              and b.bookingStatus in :statuses
              and b.startDate < :endDate
              and b.endDate > :startDate
              and (:excludeId is null or b.id <> :excludeId)
            """)
    boolean existsOverlapping(@Param("roomId") Long roomId,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              @Param("statuses") Collection<BookingStatus> statuses,
                              @Param("excludeId") Long excludeId);
}
