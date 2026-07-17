import { useMemo } from "react";
import { useAppContext } from "../context/AppContext";
import { statusText } from "../utils";

export function RoomsPage() {
  const { rooms, loadingRooms, refreshRooms } = useAppContext();

  const sortedRooms = useMemo(
    () => [...rooms].sort((a, b) => a.pricePerHour - b.pricePerHour),
    [rooms],
  );

  return (
    <section className="card page-card">
      <h2>Rooms Catalog</h2>
      <p className="muted">
        Compare capacity, location, and hourly price before booking.
      </p>
      <div className="room-grid">
        {loadingRooms ? (
          <p className="muted">Loading rooms...</p>
        ) : sortedRooms.length === 0 ? (
          <div>
            <p className="muted">
              No rooms found. Make sure backend is running.
            </p>
            <button
              type="button"
              className="secondary"
              onClick={() => void refreshRooms()}
            >
              Reload rooms
            </button>
          </div>
        ) : (
          sortedRooms.map((room) => (
            <article key={room.id} className="room-card">
              <div className="room-card-head">
                <h3>{room.name}</h3>
                <span className={`tag ${room.roomStatus.toLowerCase()}`}>
                  {statusText(room.roomStatus)}
                </span>
              </div>
              <p>{room.description || "No description provided."}</p>
              <ul>
                <li>Location: {room.location || "Not specified"}</li>
                <li>Capacity: {room.capacity} people</li>
                <li>Price: {room.pricePerHour} $ / hour</li>
              </ul>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
