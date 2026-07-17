import { useEffect, useMemo, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAppContext } from "../context/AppContext";
import type { BookingRequest } from "../types";

export function NewBookingPage() {
  const { auth, rooms, pending, loadingRooms, refreshRooms, createBooking } =
    useAppContext();

  const [bookingForm, setBookingForm] = useState<BookingRequest>({
    roomId: 0,
    startDate: "",
    endDate: "",
    note: "",
  });

  const selectedRoom = useMemo(
    () =>
      rooms.find((room) => room.id === (bookingForm.roomId || rooms[0]?.id)) ??
      null,
    [rooms, bookingForm.roomId],
  );

  useEffect(() => {
    if (rooms.length === 0) {
      void refreshRooms();
    }
  }, [rooms.length, refreshRooms]);

  if (!auth) {
    return <Navigate to="/auth" replace />;
  }

  return (
    <section className="card page-card">
      <h2>Create a New Booking</h2>
      <p className="muted">
        Choose date and room, then submit your reservation request.
      </p>

      {loadingRooms && <p className="muted">Loading rooms list...</p>}
      {!loadingRooms && rooms.length === 0 && (
        <div>
          <p className="muted">
            Rooms are unavailable right now. Start backend API and reload rooms.
          </p>
          <button
            type="button"
            className="secondary"
            onClick={() => void refreshRooms()}
          >
            Reload rooms
          </button>
        </div>
      )}

      <div className="split-grid">
        <form
          className="form-grid"
          onSubmit={async (event) => {
            event.preventDefault();
            await createBooking({
              ...bookingForm,
              roomId: bookingForm.roomId || rooms[0]?.id || 0,
            });
            setBookingForm((previous) => ({ ...previous, note: "" }));
          }}
        >
          <label>
            Room
            <select
              value={bookingForm.roomId || rooms[0]?.id || 0}
              onChange={(event) =>
                setBookingForm((previous) => ({
                  ...previous,
                  roomId: Number(event.target.value),
                }))
              }
            >
              {rooms.map((room) => (
                <option key={room.id} value={room.id}>
                  {room.name} - {room.pricePerHour}/hour
                </option>
              ))}
            </select>
          </label>
          <label>
            Start Date & Time
            <input
              type="datetime-local"
              required
              value={bookingForm.startDate}
              onChange={(event) =>
                setBookingForm((previous) => ({
                  ...previous,
                  startDate: event.target.value,
                }))
              }
            />
          </label>
          <label>
            End Date & Time
            <input
              type="datetime-local"
              required
              value={bookingForm.endDate}
              onChange={(event) =>
                setBookingForm((previous) => ({
                  ...previous,
                  endDate: event.target.value,
                }))
              }
            />
          </label>
          <label>
            Note
            <textarea
              rows={4}
              value={bookingForm.note}
              onChange={(event) =>
                setBookingForm((previous) => ({
                  ...previous,
                  note: event.target.value,
                }))
              }
              placeholder="Projector, whiteboard, extra chairs..."
            ></textarea>
          </label>
          <button className="primary" disabled={pending || rooms.length === 0}>
            Submit Booking
          </button>
        </form>

        <aside className="card room-preview">
          <h3>Selected Room</h3>
          {selectedRoom ? (
            <ul>
              <li>Name: {selectedRoom.name}</li>
              <li>Location: {selectedRoom.location || "Not specified"}</li>
              <li>Capacity: {selectedRoom.capacity} people</li>
              <li>Price: {selectedRoom.pricePerHour} per hour</li>
            </ul>
          ) : (
            <p className="muted">No room selected.</p>
          )}
        </aside>
      </div>
    </section>
  );
}
