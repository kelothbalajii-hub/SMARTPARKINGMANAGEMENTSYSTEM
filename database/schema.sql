-- =====================================================================
-- Smart Parking Management System - MySQL Schema + Sample Data
-- =====================================================================
-- NOTE: You do NOT have to run this file. Spring Boot (ddl-auto=update)
-- creates these tables automatically and DataSeeder inserts demo data.
-- This script is provided for documentation / manual setup / project report.
--
-- Run:  mysql -u root -p < database/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS smart_parking_db;
USE smart_parking_db;

-- ---------- USERS ----------
CREATE TABLE IF NOT EXISTS users (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    phone     VARCHAR(255),
    role      VARCHAR(255) NOT NULL            -- 'USER' or 'ADMIN'
);

-- ---------- VEHICLES ----------
CREATE TABLE IF NOT EXISTS vehicles (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(255) NOT NULL UNIQUE,
    vehicle_type   VARCHAR(255) NOT NULL,      -- CAR / BIKE / TRUCK
    model          VARCHAR(255),
    user_id        BIGINT NOT NULL,
    CONSTRAINT fk_vehicle_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ---------- PARKING AREAS ----------
CREATE TABLE IF NOT EXISTS parking_areas (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    location       VARCHAR(255) NOT NULL,
    total_slots    INT NOT NULL,
    price_per_hour DOUBLE NOT NULL,
    description    VARCHAR(255)
);

-- ---------- PARKING SLOTS ----------
CREATE TABLE IF NOT EXISTS parking_slots (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number     VARCHAR(255) NOT NULL,
    slot_type       VARCHAR(255) NOT NULL,     -- CAR / BIKE / TRUCK
    status          VARCHAR(255) NOT NULL,     -- AVAILABLE / RESERVED / OCCUPIED
    parking_area_id BIGINT NOT NULL,
    CONSTRAINT fk_slot_area FOREIGN KEY (parking_area_id) REFERENCES parking_areas(id)
);

-- ---------- RESERVATIONS ----------
CREATE TABLE IF NOT EXISTS reservations (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    vehicle_id       BIGINT NOT NULL,
    parking_slot_id  BIGINT NOT NULL,
    reservation_time DATETIME(6) NOT NULL,
    status           VARCHAR(255) NOT NULL,    -- ACTIVE / COMPLETED / CANCELLED
    CONSTRAINT fk_res_user    FOREIGN KEY (user_id)         REFERENCES users(id),
    CONSTRAINT fk_res_vehicle FOREIGN KEY (vehicle_id)      REFERENCES vehicles(id),
    CONSTRAINT fk_res_slot    FOREIGN KEY (parking_slot_id) REFERENCES parking_slots(id)
);

-- ---------- PARKING SESSIONS ----------
CREATE TABLE IF NOT EXISTS parking_sessions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    vehicle_id       BIGINT NOT NULL,
    parking_slot_id  BIGINT NOT NULL,
    entry_time       DATETIME(6) NOT NULL,
    exit_time        DATETIME(6),
    duration_minutes BIGINT,
    total_amount     DOUBLE,
    status           VARCHAR(255) NOT NULL,    -- ACTIVE / COMPLETED
    CONSTRAINT fk_sess_user    FOREIGN KEY (user_id)         REFERENCES users(id),
    CONSTRAINT fk_sess_vehicle FOREIGN KEY (vehicle_id)      REFERENCES vehicles(id),
    CONSTRAINT fk_sess_slot    FOREIGN KEY (parking_slot_id) REFERENCES parking_slots(id)
);

-- =====================================================================
-- SAMPLE DATA (same as DataSeeder.java)
-- =====================================================================
INSERT INTO users (name, email, password, phone, role) VALUES
 ('System Admin', 'admin@smartparking.com', 'admin123', '9000000000', 'ADMIN'),
 ('Demo User',    'demo@smartparking.com',  'demo123',  '9876543210', 'USER');

INSERT INTO parking_areas (name, location, total_slots, price_per_hour, description) VALUES
 ('Anurag University Main Parking', 'Main Block, Gate 1', 10, 20, 'Primary parking area near the main entrance.'),
 ('Library Block Parking', 'Library Building, East Wing', 8, 15, 'Parking area adjacent to the library.'),
 ('Sports Complex Parking', 'Sports Complex, South Gate', 6, 10, 'Parking area near the sports complex.');

INSERT INTO parking_slots (slot_number, slot_type, status, parking_area_id) VALUES
 ('A01','CAR','AVAILABLE',1),('A02','CAR','AVAILABLE',1),('A03','CAR','AVAILABLE',1),
 ('A04','CAR','AVAILABLE',1),('A05','CAR','AVAILABLE',1),('A06','CAR','RESERVED',1),
 ('A07','CAR','AVAILABLE',1),('A08','CAR','OCCUPIED',1),('A09','CAR','AVAILABLE',1),
 ('A10','CAR','AVAILABLE',1),
 ('B01','CAR','AVAILABLE',2),('B02','CAR','AVAILABLE',2),('B03','CAR','AVAILABLE',2),
 ('B04','CAR','AVAILABLE',2),('B05','BIKE','AVAILABLE',2),('B06','BIKE','AVAILABLE',2),
 ('B07','BIKE','AVAILABLE',2),('B08','BIKE','AVAILABLE',2),
 ('C01','CAR','AVAILABLE',3),('C02','CAR','AVAILABLE',3),('C03','CAR','AVAILABLE',3),
 ('C04','BIKE','AVAILABLE',3),('C05','BIKE','AVAILABLE',3),('C06','BIKE','AVAILABLE',3);

-- Useful report queries
-- Total revenue:       SELECT SUM(total_amount) FROM parking_sessions WHERE status='COMPLETED';
-- Slots by status:     SELECT status, COUNT(*) FROM parking_slots GROUP BY status;
-- Revenue per area:    SELECT pa.name, SUM(ps.total_amount) FROM parking_sessions ps
--                      JOIN parking_slots s ON ps.parking_slot_id=s.id
--                      JOIN parking_areas pa ON s.parking_area_id=pa.id
--                      WHERE ps.status='COMPLETED' GROUP BY pa.name;
