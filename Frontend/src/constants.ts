import type { BookingStatus } from './types'

export const TOKEN_KEY = 'conference-booking-token'
export const AUTH_KEY = 'conference-booking-auth'

export const BOOKING_STATUS_OPTIONS: BookingStatus[] = [
  'PENDING',
  'CONFIRMED',
  'CANCELLED',
  'REJECTED',
  'COMPLETED',
]
