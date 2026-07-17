import { NavLink, Outlet } from "react-router-dom";
import { MessageBanner } from "./MessageBanner";
import { useAppContext } from "../context/AppContext";
import { statusText } from "../utils";

function linkClass({ isActive }: { isActive: boolean }) {
  return isActive ? "nav-link active" : "nav-link";
}

export function ShellLayout() {
  const { auth, currentUser, isAdminOrManager, logout } = useAppContext();

  return (
    <div className="app-shell">
      <aside className="sidebar card">
        <div>
          <p className="eyebrow">Conference Flow</p>
          <h1>Room Booking</h1>
          <p className="muted">Manage meetings, rooms, and reservations.</p>
        </div>

        <nav className="nav-group">
          <NavLink to="/" end className={linkClass}>
            Overview
          </NavLink>
          <NavLink to="/rooms" className={linkClass}>
            Rooms
          </NavLink>
          <NavLink to="/bookings/new" className={linkClass}>
            New Booking
          </NavLink>
          <NavLink to="/bookings/my" className={linkClass}>
            My Bookings
          </NavLink>
          <NavLink to="/account" className={linkClass}>
            Account
          </NavLink>
          {!auth && (
            <NavLink to="/auth" className={linkClass}>
              Sign In / Register
            </NavLink>
          )}
          {isAdminOrManager && (
            <NavLink to="/admin/bookings" className={linkClass}>
              Admin Board
            </NavLink>
          )}
        </nav>

        <div className="sidebar-footer">
          {auth ? (
            <>
              <p className="muted">
                Signed in as{" "}
                {currentUser
                  ? `${currentUser.firstName} ${currentUser.lastName}`
                  : auth.email}
              </p>
              <p className="role-pill">{statusText(auth.role)}</p>
              <button type="button" className="ghost" onClick={logout}>
                Sign Out
              </button>
            </>
          ) : (
            <p className="muted">Guest mode: browse rooms and availability.</p>
          )}
        </div>
      </aside>

      <section className="content-column">
        <MessageBanner />
        <Outlet />
      </section>
    </div>
  );
}
