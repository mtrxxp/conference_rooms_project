import type {
  AuthResponse,
  BookingRequest,
  BookingResponse,
  BookingStatusUpdateRequest,
  ErrorResponse,
  LoginRequest,
  RegisterRequest,
  RoomResponse,
  UserResponse,
} from './types'

function normalizeApiBase(rawBase?: string): string {
  const fallback = 'http://localhost:8080/api'
  const base = (rawBase ?? '').trim() || fallback
  const noTrailingSlash = base.replace(/\/+$/, '')
  return noTrailingSlash.endsWith('/api') ? noTrailingSlash : `${noTrailingSlash}/api`
}

const API_BASE = normalizeApiBase(import.meta.env.VITE_API_BASE_URL)

export class ApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(message: string, status = 500, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

interface RequestInitEx extends RequestInit {
  token?: string
}

async function request<T>(path: string, init: RequestInitEx = {}): Promise<T> {
  const { token, headers, ...rest } = init
  const requestHeaders: Record<string, string> = {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }

  if (headers) {
    Object.assign(requestHeaders, headers as Record<string, string>)
  }

  if (rest.body && !(rest.body instanceof FormData)) {
    requestHeaders['Content-Type'] = 'application/json'
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...rest,
    headers: requestHeaders,
  })

  if (!response.ok) {
    let errorBody: ErrorResponse | null = null
    try {
      errorBody = (await response.json()) as ErrorResponse
    } catch {
      // Ignore non-JSON error payloads and use fallback message.
    }

    throw new ApiError(
      errorBody?.message ?? `HTTP error ${response.status}`,
      response.status,
      errorBody?.fieldErrors ?? {},
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const api = {
  login(payload: LoginRequest) {
    return request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  register(payload: RegisterRequest) {
    return request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  getCurrentUser(token: string) {
    return request<UserResponse>('/users/me', { token })
  },

  getRooms() {
    return request<RoomResponse[]>('/rooms')
  },

  getMyBookings(token: string) {
    return request<BookingResponse[]>('/bookings/my', { token })
  },

  getAllBookings(token: string) {
    return request<BookingResponse[]>('/bookings', { token })
  },

  createBooking(payload: BookingRequest, token: string) {
    return request<BookingResponse>('/bookings', {
      method: 'POST',
      body: JSON.stringify(payload),
      token,
    })
  },

  cancelBooking(id: number, token: string) {
    return request<void>(`/bookings/${id}`, {
      method: 'DELETE',
      token,
    })
  },

  updateBookingStatus(id: number, payload: BookingStatusUpdateRequest, token: string) {
    return request<BookingResponse>(`/bookings/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
      token,
    })
  },
}
