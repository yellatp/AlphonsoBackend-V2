-- Database creation script for AlphonsoBackend-V2
-- Run this script if databases need to be created manually:
-- mysql -u root -p < create-databases.sql

CREATE DATABASE IF NOT EXISTS authd;
CREATE DATABASE IF NOT EXISTS moodle_service_db;
CREATE DATABASE IF NOT EXISTS Interviewer;

-- Verify databases were created
SHOW DATABASES;
