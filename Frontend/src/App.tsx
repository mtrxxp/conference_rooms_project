import { Navigate, Route, Routes } from "react-router-dom";
import { ShellLayout } from "./components/ShellLayout";
import { AppProvider } from "./context/AppContext";
import { AccountPage } from "./pages/AccountPage";
import { AdminBookingsPage } from "./pages/AdminBookingsPage";
import { AuthPage } from "./pages/AuthPage";
import { DashboardPage } from "./pages/DashboardPage";
import { MyBookingsPage } from "./pages/MyBookingsPage";
import { NewBookingPage } from "./pages/NewBookingPage";
import { RoomsPage } from "./pages/RoomsPage";
import "./App.css";

function App() {
  return (
    <AppProvider>
      <Routes>
        <Route path="/" element={<ShellLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="auth" element={<AuthPage />} />
          <Route path="rooms" element={<RoomsPage />} />
          <Route path="bookings/new" element={<NewBookingPage />} />
          <Route path="bookings/my" element={<MyBookingsPage />} />
          <Route path="admin/bookings" element={<AdminBookingsPage />} />
          <Route path="account" element={<AccountPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </AppProvider>
  );
}

export default App;
