# Smart Parking Management System

Full-stack project: **React (Vite)** frontend + **Spring Boot 3 (Java 21)** backend + **MySQL**.

## Features
- User: register/login, manage vehicles, browse areas & slots, reserve a slot, vehicle entry/exit, live fee, parking history
- Admin: dashboard stats, users, vehicles, parking areas, slots, reservations (cancel), active sessions (force exit), transactions & revenue

## Setup
1. **MySQL** – make sure it is running. Update username/password in `backend/src/main/resources/application.properties` (default `root` / `root`). The database `smart_parking_db` is created automatically (optional manual script: `database/schema.sql`).
2. **Backend**
   ```
   cd backend
   mvn spring-boot:run
   ```
   Runs on http://localhost:8080 — demo data is seeded on first start.
3. **Frontend**
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Runs on http://localhost:5173

## Demo logins
| Role  | Email                  | Password |
|-------|------------------------|----------|
| Admin | admin@smartparking.com | admin123 |
| User  | demo@smartparking.com  | demo123  |

## Demo workflow
Register vehicle → Parking Areas → pick slot → Reserve → Active Session → Record Entry → Record Exit (fee = rounded-up hours × area price) → History / Admin Transactions.

## Project structure
```
backend/   Spring Boot (controller → service → repository → entity)
frontend/  React pages, components, Axios API layer
database/  schema.sql
```
