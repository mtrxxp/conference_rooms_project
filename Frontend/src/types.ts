export type Role = 'USER' | 'MANAGER' | 'ADMIN'

export type RoomStatus = 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'UNAVAILABLE'

export type BookingStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'REJECTED'
  | 'COMPLETED'

export interface AuthResponse {
  token: string
  tokenType: string
  userId: number
  email: string
  role: Role
}

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  phoneNumber: string | null
  role: Role
  createdDate: string
}

export interface RoomResponse {
  id: number
  name: string
  location: string
  capacity: number
  pricePerHour: number
  description: string
  roomStatus: RoomStatus
  createdDate: string
}

export interface BookingResponse {
  id: number
  userId: number
  userEmail: string
  roomId: number
  roomName: string
  startDate: string
  endDate: string
  price: number
  bookingStatus: BookingStatus
  note: string | null
  createdDate: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  phoneNumber: string
}

export interface BookingRequest {
  roomId: number
  startDate: string
  endDate: string
  note: string
}

export interface BookingStatusUpdateRequest {
  bookingStatus: BookingStatus
}

export interface ErrorResponse {
  status?: number
  error?: string
  message?: string
  path?: string
  fieldErrors?: Record<string, string>
}
