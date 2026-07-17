# Conference Rooms Booking System

A full-stack web application for managing conference room reservations. Users can browse available rooms, create bookings, and manage their reservations. Managers and admins have additional controls to oversee all bookings and maintain the room catalog.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, TypeScript, Vite, React Router 7 |
| Backend | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16 |
| Auth | JWT (stateless, stored in localStorage) |
| Infrastructure | Docker, Docker Compose, Nginx |

## Features

**For Users**
- Register and log in with email/password
- Browse conference rooms with details (capacity, location, price per hour)
- Create bookings with automatic price calculation
- View and cancel personal bookings

**For Managers**
- View all bookings across all users
- Update booking statuses (Confirm, Reject, Complete)

**For Admins**
- Full room management (create, update, delete)
- Full user management (view, update roles, delete)
- All manager capabilities

## Project Structure

```
conference_rooms_booking_project/
├── Backend/                    # Spring Boot application
│   └── src/main/java/.../
│       ├── auth/               # Registration & login
│       ├── booking/            # Booking logic & validation
│       ├── room/               # Room management
│       ├── user/               # User profile management
│       ├── security/           # JWT & Spring Security config
│       └── config/             # Demo data initialization
│
├── Frontend/                   # React + TypeScript application
│   └── src/
│       ├── pages/              # Page components
│       ├── components/         # Reusable UI components
│       ├── context/            # Global app state (AppContext)
│       ├── api.ts              # Centralized API client
│       └── types.ts            # Shared TypeScript types
│
└── docker-compose.yml          # Full-stack orchestration
```

## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose

### Run with Docker (Recommended)

```bash
git clone <repository-url>
cd conference_rooms_booking_project
docker-compose up --build
```

The following services will start:

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| PostgreSQL | localhost:5432 |

### Run Manually

**Backend** (requires Java 21 and a running PostgreSQL instance):

```bash
cd Backend
./mvnw spring-boot:run
```

**Frontend** (requires Node.js 20+):

```bash
cd Frontend
npm install
npm run dev
```

## Default Accounts

The application auto-creates these accounts on first startup:

| Role | Email | Password |
|---|---|---|
| Admin | admin@conference.local | Admin123! |
| Manager | manager@conference.local | Manager123! |

## Configuration

### Backend (`Backend/src/main/resources/application.properties`)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/conference
spring.datasource.username=postgres
spring.datasource.password=0000

# JWT
app.jwt.secret=<your-secret>
app.jwt.expiration-ms=86400000   # 24 hours
```

### Frontend

Set `VITE_API_BASE_URL` to point to the backend if it's not running on the default `http://localhost:8080/api`.

## API Overview

### Authentication (Public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a new account |
| POST | `/api/auth/login` | Log in and receive a JWT |

### Rooms

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/rooms` | Public |
| GET | `/api/rooms/{id}` | Public |
| POST | `/api/rooms` | Admin |
| PUT | `/api/rooms/{id}` | Admin |
| DELETE | `/api/rooms/{id}` | Admin |

### Bookings

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/bookings` | Admin / Manager |
| GET | `/api/bookings/my` | Authenticated |
| GET | `/api/bookings/{id}` | Owner / Admin / Manager |
| POST | `/api/bookings` | Authenticated |
| PUT | `/api/bookings/{id}` | Owner (PENDING only) |
| PATCH | `/api/bookings/{id}/status` | Admin / Manager |
| DELETE | `/api/bookings/{id}` | Owner |

### Users

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/users` | Admin / Manager |
| GET | `/api/users/me` | Authenticated |
| PUT | `/api/users/{id}` | Owner |
| PATCH | `/api/users/{id}/role` | Admin |
| DELETE | `/api/users/{id}` | Admin |

## Data Model

```
User (1) ──< Booking >── (1) Room
```

**User** — id, firstName, lastName, email, password (BCrypt), phoneNumber, role (USER / MANAGER / ADMIN)

**Room** — id, name, location, capacity, pricePerHour, description, status (AVAILABLE / OCCUPIED / MAINTENANCE / UNAVAILABLE)

**Booking** — id, user, room, startDate, endDate, price (auto-calculated), status (PENDING / CONFIRMED / CANCELLED / REJECTED / COMPLETED), note

Key constraints:
- A room cannot have two overlapping PENDING or CONFIRMED bookings
- Bookings can only be edited while in PENDING status
- Price is calculated automatically as `hours × pricePerHour`

## Running Tests

```bash
cd Backend
./mvnw test
```

Tests cover booking service logic (overlap validation, price calculation, ownership rules), auth service, room service, user service, and JWT utilities using JUnit 5 and Mockito.

## Demo Rooms

Five conference rooms are pre-loaded on first startup:

| Room | Location | Capacity | Price/hr | Status |
|---|---|---|---|---|
| Aurora | Floor 1, Wing A | 6 | $150 | Available |
| Borealis | Floor 2, Wing B | 12 | $300 | Available |
| Cosmos | Floor 3 | 30 | $700 | Available |
| Delta | Floor 1, Wing C | 4 | $100 | Available |
| Everest | Floor 4 | 50 | $1,200 | Maintenance |
