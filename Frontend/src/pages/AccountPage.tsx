import { Link } from "react-router-dom";
import { useAppContext } from "../context/AppContext";
import { statusText } from "../utils";

export function AccountPage() {
  const { auth, currentUser } = useAppContext();

  return (
    <section className="card page-card">
      <h2>Account</h2>
      {!auth ? (
        <>
          <p className="muted">You are browsing as a guest.</p>
          <Link className="primary action-link" to="/auth">
            Go to Sign In
          </Link>
        </>
      ) : currentUser ? (
        <div className="profile-grid">
          <article className="card profile-item">
            <h3>Personal Info</h3>
            <ul>
              <li>First Name: {currentUser.firstName}</li>
              <li>Last Name: {currentUser.lastName}</li>
              <li>Email: {currentUser.email}</li>
              <li>Phone: {currentUser.phoneNumber || "Not specified"}</li>
            </ul>
          </article>

          <article className="card profile-item">
            <h3>Role & Access</h3>
            <ul>
              <li>Role: {statusText(currentUser.role)}</li>
              <li>
                Permissions:{" "}
                {currentUser.role === "ADMIN"
                  ? "Full management"
                  : "Booking only"}
              </li>
            </ul>
          </article>
        </div>
      ) : (
        <p className="muted">Loading profile...</p>
      )}
    </section>
  );
}
