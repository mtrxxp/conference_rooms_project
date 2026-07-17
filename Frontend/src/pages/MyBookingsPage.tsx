import { Navigate } from "react-router-dom";
import { useAppContext } from "../context/AppContext";
import { formatDate, statusText } from "../utils";

export function MyBookingsPage() {
  const { auth, myBookings, loadingBookings, pending, cancelBooking } =
    useAppContext();

  if (!auth) {
    return <Navigate to="/auth" replace />;
  }

  return (
    <section className="card page-card">
      <h2>My Bookings</h2>
      <p className="muted">
        Track all your reservations and cancel active ones if needed.
      </p>

      {loadingBookings ? (
        <p className="muted">Loading reservations...</p>
      ) : myBookings.length === 0 ? (
        <p className="muted">You do not have any bookings yet.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Room</th>
                <th>Period</th>
                <th>Status</th>
                <th>Price</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {myBookings.map((booking) => (
                <tr key={booking.id}>
                  <td>{booking.roomName}</td>
                  <td>
                    {formatDate(booking.startDate)} -{" "}
                    {formatDate(booking.endDate)}
                  </td>
                  <td>{statusText(booking.bookingStatus)}</td>
                  <td>{booking.price}</td>
                  <td>
                    {booking.bookingStatus === "PENDING" ||
                    booking.bookingStatus === "CONFIRMED" ? (
                      <button
                        type="button"
                        className="danger"
                        onClick={() => void cancelBooking(booking.id)}
                        disabled={pending}
                      >
                        Cancel
                      </button>
                    ) : (
                      <span className="muted">-</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
