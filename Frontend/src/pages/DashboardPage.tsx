import { Link } from "react-router-dom";
import { useAppContext } from "../context/AppContext";

export function DashboardPage() {
  const { rooms, myBookings, allBookings, isAdmin, auth } = useAppContext();

  const availableRooms = rooms.filter(
    (room) => room.roomStatus === "AVAILABLE",
  ).length;

  return (
    <div className="stack">
      <section className="card hero-card">
        <p className="eyebrow">Workspace Overview</p>
        <h2>Plan your meetings with confidence</h2>
        <p className="muted">
          Split workflow into clear sections: room discovery, booking creation,
          personal schedule, and admin moderation.
        </p>
        <div className="actions-row">
          <Link className="primary action-link" to="/rooms">
            Explore Rooms
          </Link>
          <Link
            className="secondary action-link"
            to={auth ? "/bookings/new" : "/auth"}
          >
            {auth ? "Create Booking" : "Sign In to Book"}
          </Link>
        </div>
      </section>

      <section className="stats-grid">
        <article className="card stat-card">
          <p>Total Rooms</p>
          <h3>{rooms.length}</h3>
        </article>
        <article className="card stat-card">
          <p>Available Right Now</p>
          <h3>{availableRooms}</h3>
        </article>
        <article className="card stat-card">
          <p>My Bookings</p>
          <h3>{myBookings.length}</h3>
        </article>
        <article className="card stat-card">
          <p>{isAdmin ? "All Reservations" : "Account Status"}</p>
          <h3>{isAdmin ? allBookings.length : auth ? "Active" : "Guest"}</h3>
        </article>
      </section>
    </div>
  );
}
