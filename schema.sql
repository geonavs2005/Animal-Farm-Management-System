-- =======================================================
-- Animal Farm Management System - Complete Database Schema
-- Database: animal_farm_db
-- =======================================================

DROP DATABASE IF EXISTS animal_farm_db;
CREATE DATABASE animal_farm_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE animal_farm_db;

-- -------------------------------------------------------
-- 1. USERS TABLE
-- -------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    full_name VARCHAR(100) DEFAULT '',
    email VARCHAR(100) DEFAULT '',
    farm_type VARCHAR(100) DEFAULT 'General Livestock',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO users (username, password, role, full_name, email, farm_type) VALUES
('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@farm.local', 'Headquarters'),
('farmer1', 'password123', 'USER', 'Farmer 1', 'farmer1@farm.local', 'Dairy & Cattle'),
('geo', '1234geo', 'USER', 'Geona', 'geonavs7@gmail.com', 'Dairy & Cattle'),
('abhil', 'abhil@gmail.com', 'USER', 'ABHIL', 'abhil@gmail.com', 'Dairy & Cattle'),
('deno', 'deno@gmail.com', 'USER', 'deno', 'deno@gmail.com', 'Poultry & Eggs'),
('ayana', 'ayana123', 'USER', 'ayana', 'ayana@farm.local', 'Poultry & Eggs'),
('sayana', 'sayana123', 'USER', 'sayana', 'sayana@farm.local', 'Dairy & Cattle');

-- -------------------------------------------------------
-- 2. ANIMALS TABLE
-- -------------------------------------------------------
CREATE TABLE animals (
    animal_id INT AUTO_INCREMENT PRIMARY KEY,
    tag VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(50) DEFAULT '',
    age VARCHAR(50) DEFAULT '',
    gender VARCHAR(20) DEFAULT 'Female',
    health VARCHAR(50) DEFAULT 'Healthy',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO animals (tag, name, species, breed, age, gender, health) VALUES
('AN-101', 'Daisy', 'Cattle', 'Holstein', '3.5 yrs', 'Female', 'Healthy'),
('AN-102', 'Barnaby', 'Sheep', 'Merino', '2 yrs', 'Male', 'Healthy'),
('AN-103', 'Billy', 'Goat', 'Boer', '1.5 yrs', 'Male', 'Under Treatment'),
('AN-104', 'Thunder', 'Horse', 'Quarter Horse', '5 yrs', 'Male', 'Healthy'),
('AN-105', 'Pippa', 'Pig', 'Yorkshire', '1 yr', 'Female', 'Healthy'),
('AN-106', 'Rusty', 'Poultry', 'Rhode Island Red', '8 mos', 'Female', 'Quarantine');

-- -------------------------------------------------------
-- 3. FEED INVENTORY TABLE
-- -------------------------------------------------------
CREATE TABLE feed (
    feed_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    stock DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    consumption DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    last_updated DATE NOT NULL
) ENGINE=InnoDB;

INSERT INTO feed (code, name, stock, consumption, last_updated) VALUES
('FD-01', 'Alfalfa Green Hay', 450.00, 25.00, '2026-10-06'),
('FD-02', 'Cattle Protein Pellets', 120.00, 28.00, '2026-10-05'),
('FD-03', 'Organic Grain Blend', 65.00, 20.00, '2026-10-07'),
('FD-04', 'Mineral & Salt Lick', 85.00, 4.00, '2026-10-04');

-- -------------------------------------------------------
-- 4. HEALTH & MEDICAL RECORDS TABLE
-- -------------------------------------------------------
CREATE TABLE health_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    record_id VARCHAR(50) UNIQUE NOT NULL,
    animal VARCHAR(100) NOT NULL,
    treatment_type VARCHAR(100) NOT NULL,
    veterinarian VARCHAR(100) DEFAULT '',
    record_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'Completed',
    notes TEXT
) ENGINE=InnoDB;

INSERT INTO health_records (record_id, animal, treatment_type, veterinarian, record_date, status, notes) VALUES
('HL-201', 'AN-101 (Daisy)', 'Annual Anthrax Booster', 'Dr. Miller', '2026-09-28', 'Completed', 'Normal vital signs after vaccination.'),
('HL-202', 'AN-103 (Billy)', 'Hoof Rot Treatment', 'Dr. Sarah Lee', '2026-10-03', 'Pending', 'Topical copper sulfate applied; re-check in 5 days.'),
('HL-203', 'AN-106 (Rusty)', 'Avian Respiratory Isolation', 'Dr. Miller', '2026-10-05', 'Pending', 'Placed in quarantine coop #3.');

-- -------------------------------------------------------
-- 5. BREEDING TABLE
-- -------------------------------------------------------
CREATE TABLE breeding (
    breeding_id INT AUTO_INCREMENT PRIMARY KEY,
    female_animal_tag VARCHAR(50),
    male_animal_tag VARCHAR(50),
    breeding_date DATE,
    expected_date DATE,
    breeding_status VARCHAR(50),
    notes VARCHAR(255)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 6. PRODUCTION TABLE
-- -------------------------------------------------------
CREATE TABLE production (
    production_id INT AUTO_INCREMENT PRIMARY KEY,
    animal_tag VARCHAR(50),
    production_date DATE,
    production_type VARCHAR(50),
    quantity DECIMAL(10,2),
    unit VARCHAR(20),
    notes VARCHAR(255)
) ENGINE=InnoDB;

