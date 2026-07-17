import { useState } from "react";
import { Navigate } from "react-router-dom";
import { useAppContext } from "../context/AppContext";
import type { RegisterRequest } from "../types";

export function AuthPage() {
  const { auth, pending, login, register } = useAppContext();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState<RegisterRequest>({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phoneNumber: "",
  });

  if (auth) {
    return <Navigate to="/account" replace />;
  }

  return (
    <section className="card page-card auth-page">
      <h2>Authentication</h2>
      <p className="muted">Create account or sign in to manage reservations.</p>

      <div className="auth-switch">
        <button
          type="button"
          className={mode === "login" ? "active" : ""}
          onClick={() => setMode("login")}
        >
          Sign In
        </button>
        <button
          type="button"
          className={mode === "register" ? "active" : ""}
          onClick={() => setMode("register")}
        >
          Register
        </button>
      </div>

      {mode === "login" ? (
        <form
          className="form-grid"
          onSubmit={async (event) => {
            event.preventDefault();
            await login(loginForm);
            setLoginForm({ email: "", password: "" });
          }}
        >
          <label>
            Email
            <input
              type="email"
              required
              value={loginForm.email}
              onChange={(event) =>
                setLoginForm((previous) => ({
                  ...previous,
                  email: event.target.value,
                }))
              }
            />
          </label>
          <label>
            Password
            <input
              type="password"
              required
              value={loginForm.password}
              onChange={(event) =>
                setLoginForm((previous) => ({
                  ...previous,
                  password: event.target.value,
                }))
              }
            />
          </label>
          <button className="primary" disabled={pending}>
            Sign In
          </button>
        </form>
      ) : (
        <form
          className="form-grid"
          onSubmit={async (event) => {
            event.preventDefault();
            await register(registerForm);
            setRegisterForm({
              firstName: "",
              lastName: "",
              email: "",
              password: "",
              phoneNumber: "",
            });
          }}
        >
          <label>
            First Name
            <input
              type="text"
              required
              value={registerForm.firstName}
              onChange={(event) =>
                setRegisterForm((previous) => ({
                  ...previous,
                  firstName: event.target.value,
                }))
              }
            />
          </label>
          <label>
            Last Name
            <input
              type="text"
              required
              value={registerForm.lastName}
              onChange={(event) =>
                setRegisterForm((previous) => ({
                  ...previous,
                  lastName: event.target.value,
                }))
              }
            />
          </label>
          <label>
            Phone Number
            <input
              type="tel"
              value={registerForm.phoneNumber}
              onChange={(event) =>
                setRegisterForm((previous) => ({
                  ...previous,
                  phoneNumber: event.target.value,
                }))
              }
            />
          </label>
          <label>
            Email
            <input
              type="email"
              required
              value={registerForm.email}
              onChange={(event) =>
                setRegisterForm((previous) => ({
                  ...previous,
                  email: event.target.value,
                }))
              }
            />
          </label>
          <label>
            Password
            <input
              type="password"
              minLength={6}
              required
              value={registerForm.password}
              onChange={(event) =>
                setRegisterForm((previous) => ({
                  ...previous,
                  password: event.target.value,
                }))
              }
            />
          </label>
          <button className="primary" disabled={pending}>
            Create Account
          </button>
        </form>
      )}
    </section>
  );
}
