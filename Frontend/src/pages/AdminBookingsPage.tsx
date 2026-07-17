import { Navigate } from "react-router-dom";
import { BOOKING_STATUS_OPTIONS } from "../constants";
import { useAppContext } from "../context/AppContext";
import type { BookingStatus } from "../types";
import { formatDate, statusText } from "../utils";

export function AdminBookingsPage() {
  const {
    auth,
    isAdminOrManager,
    allBookings,
    loadingBookings,
    pending,
    updateBookingStatus,
  } = useAppContext();

  if (!auth) {
    return <Navigate to="/auth" replace />;
  }

  if (!isAdminOrManager) {
    return (
      <section className="card page-card">
        <h2>Admin Board</h2>
        <p className="muted">You do not have permission to access this page.</p>
      </section>
    );
  }

  return (
    <section className="card page-card">
      <h2>Admin Board</h2>
      <p className="muted">
        Moderate all booking requests and update their status.
      </p>

      {loadingBookings ? (
        <p className="muted">Loading reservations...</p>
      ) : allBookings.length === 0 ? (
        <p className="muted">No booking records available.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Room</th>
                <th>Period</th>
                <th>Current Status</th>
                <th>Update Status</th>
              </tr>
            </thead>
            <tbody>
              {allBookings.map((booking) => (
                <tr key={booking.id}>
                  <td>{booking.userEmail}</td>
                  <td>{booking.roomName}</td>
                  <td>
                    {formatDate(booking.startDate)} -{" "}
                    {formatDate(booking.endDate)}
                  </td>
                  <td>{statusText(booking.bookingStatus)}</td>
                  <td>
                    <select
                      value={booking.bookingStatus}
                      onChange={(event) =>
                        void updateBookingStatus(
                          booking.id,
                          event.target.value as BookingStatus,
                        )
                      }
                      disabled={pending}
                    >
                      {BOOKING_STATUS_OPTIONS.map((status) => (
                        <option key={status} value={status}>
                          {statusText(status)}
                        </option>
                      ))}
                    </select>
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
