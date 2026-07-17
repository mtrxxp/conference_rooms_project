/* eslint-disable react-refresh/only-export-components */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { ApiError, api } from "../api";
import { AUTH_KEY, TOKEN_KEY } from "../constants";
import type {
  AuthResponse,
  BookingRequest,
  BookingResponse,
  BookingStatus,
  RegisterRequest,
  Role,
  RoomResponse,
  UserResponse,
} from "../types";
import { toBackendDateTime } from "../utils";

type Message = { type: "success" | "error"; text: string } | null;

interface AppContextValue {
  rooms: RoomResponse[];
  myBookings: BookingResponse[];
  allBookings: BookingResponse[];
  currentUser: UserResponse | null;
  auth: AuthResponse | null;
  token: string;
  role: Role | null;
  isAdmin: boolean;
  pending: boolean;
  loadingRooms: boolean;
  loadingBookings: boolean;
  message: Message;
  clearMessage: () => void;
  login: (payload: { email: string; password: string }) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshRooms: () => Promise<void>;
  refreshPrivateData: () => Promise<void>;
  createBooking: (payload: BookingRequest) => Promise<void>;
  cancelBooking: (bookingId: number) => Promise<void>;
  updateBookingStatus: (
    bookingId: number,
    bookingStatus: BookingStatus,
  ) => Promise<void>;
}

const AppContext = createContext<AppContextValue | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [myBookings, setMyBookings] = useState<BookingResponse[]>([]);
  const [allBookings, setAllBookings] = useState<BookingResponse[]>([]);
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null);

  const [token, setToken] = useState<string>(
    () => localStorage.getItem(TOKEN_KEY) ?? "",
  );
  const [auth, setAuth] = useState<AuthResponse | null>(() => {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthResponse;
    } catch {
      localStorage.removeItem(AUTH_KEY);
      return null;
    }
  });

  const [pending, setPending] = useState(false);
  const [loadingRooms, setLoadingRooms] = useState(false);
  const [loadingBookings, setLoadingBookings] = useState(false);
  const [message, setMessage] = useState<Message>(null);

  const role = auth?.role ?? null;
  const isAdmin = role === "ADMIN";

  const clearMessage = useCallback(() => {
    setMessage(null);
  }, []);

  const showError = useCallback((error: unknown) => {
    if (error instanceof ApiError) {
      const fields = Object.values(error.fieldErrors);
      if (fields.length > 0) {
        setMessage({
          type: "error",
          text: `${error.message}. ${fields.join("; ")}`,
        });
        return;
      }
      setMessage({ type: "error", text: error.message });
      return;
    }
    setMessage({ type: "error", text: "Unexpected error. Please try again." });
  }, []);

  const logout = useCallback(() => {
    setToken("");
    setAuth(null);
    setCurrentUser(null);
    setMyBookings([]);
    setAllBookings([]);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(AUTH_KEY);
  }, []);

  const refreshRooms = useCallback(async () => {
    setLoadingRooms(true);
    try {
      const response = await api.getRooms();
      setRooms(response);
    } catch (error) {
      showError(error);
    } finally {
      setLoadingRooms(false);
    }
  }, [showError]);

  const refreshPrivateData = useCallback(async () => {
    if (!token || !role) {
      return;
    }

    setLoadingBookings(true);
    try {
      const [profile, mine] = await Promise.all([
        api.getCurrentUser(token),
        api.getMyBookings(token),
      ]);
      setCurrentUser(profile);
      setMyBookings(mine);

      if (role === "ADMIN") {
        const bookings = await api.getAllBookings(token);
        setAllBookings(bookings);
      } else {
        setAllBookings([]);
      }
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        logout();
        setMessage({
          type: "error",
          text: "Session expired. Please sign in again.",
        });
      } else {
        showError(error);
      }
    } finally {
      setLoadingBookings(false);
    }
  }, [token, role, logout, showError]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void refreshRooms();
  }, [refreshRooms]);

  useEffect(() => {
    if (token && role) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      void refreshPrivateData();
    }
  }, [token, role, refreshPrivateData]);

  const persistAuth = useCallback((payload: AuthResponse) => {
    setAuth(payload);
    setToken(payload.token);
    localStorage.setItem(TOKEN_KEY, payload.token);
    localStorage.setItem(AUTH_KEY, JSON.stringify(payload));
  }, []);

  const login = useCallback(
    async (payload: { email: string; password: string }) => {
      clearMessage();
      setPending(true);
      try {
        const response = await api.login(payload);
        persistAuth(response);
        setMessage({ type: "success", text: "You are signed in." });
      } catch (error) {
        showError(error);
      } finally {
        setPending(false);
      }
    },
    [clearMessage, persistAuth, showError],
  );

  const register = useCallback(
    async (payload: RegisterRequest) => {
      clearMessage();
      setPending(true);
      try {
        const response = await api.register(payload);
        persistAuth(response);
        setMessage({ type: "success", text: "Account created successfully." });
      } catch (error) {
        showError(error);
      } finally {
        setPending(false);
      }
    },
    [clearMessage, persistAuth, showError],
  );

  const createBooking = useCallback(
    async (payload: BookingRequest) => {
      if (!token) {
        setMessage({ type: "error", text: "Please sign in first." });
        return;
      }

      if (payload.endDate <= payload.startDate) {
        setMessage({
          type: "error",
          text: "End time must be later than start time.",
        });
        return;
      }

      clearMessage();
      setPending(true);
      try {
        await api.createBooking(
          {
            ...payload,
            startDate: toBackendDateTime(payload.startDate),
            endDate: toBackendDateTime(payload.endDate),
          },
          token,
        );
        setMessage({
          type: "success",
          text: "Booking request has been created.",
        });
        await refreshPrivateData();
      } catch (error) {
        showError(error);
      } finally {
        setPending(false);
      }
    },
    [token, clearMessage, refreshPrivateData, showError],
  );

  const cancelBooking = useCallback(
    async (bookingId: number) => {
      if (!token) {
        return;
      }

      clearMessage();
      setPending(true);
      try {
        await api.cancelBooking(bookingId, token);
        setMessage({ type: "success", text: "Booking has been cancelled." });
        await refreshPrivateData();
      } catch (error) {
        showError(error);
      } finally {
        setPending(false);
      }
    },
    [token, clearMessage, refreshPrivateData, showError],
  );

  const updateBookingStatus = useCallback(
    async (bookingId: number, bookingStatus: BookingStatus) => {
      if (!token) {
        return;
      }

      clearMessage();
      setPending(true);
      try {
        await api.updateBookingStatus(bookingId, { bookingStatus }, token);
        setMessage({
          type: "success",
          text: "Booking status has been updated.",
        });
        await refreshPrivateData();
      } catch (error) {
        showError(error);
      } finally {
        setPending(false);
      }
    },
    [token, clearMessage, refreshPrivateData, showError],
  );

  const value = useMemo<AppContextValue>(
    () => ({
      rooms,
      myBookings,
      allBookings,
      currentUser,
      auth,
      token,
      role,
      isAdmin,
      pending,
      loadingRooms,
      loadingBookings,
      message,
      clearMessage,
      login,
      register,
      logout,
      refreshRooms,
      refreshPrivateData,
      createBooking,
      cancelBooking,
      updateBookingStatus,
    }),
    [
      rooms,
      myBookings,
      allBookings,
      currentUser,
      auth,
      token,
      role,
      isAdmin,
      pending,
      loadingRooms,
      loadingBookings,
      message,
      clearMessage,
      login,
      register,
      logout,
      refreshRooms,
      refreshPrivateData,
      createBooking,
      cancelBooking,
      updateBookingStatus,
    ],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext(): AppContextValue {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useAppContext must be used inside AppProvider");
  }
  return context;
}
