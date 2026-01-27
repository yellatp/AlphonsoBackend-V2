-- Script to delete ALL data from ALL databases
-- This script preserves table structures but deletes all data
-- WARNING: This will delete all data from all databases!
-- 
-- Usage: mysql -u root -p < delete-all-data.sql

-- Disable safe update mode and foreign key checks
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- DATABASE: authd (User-Service & Profile-Service)
-- ============================================
USE authd;

-- Delete from junction tables first
DELETE FROM profile_core_skills;
DELETE FROM profile_additional_skills;

-- Delete from dependent tables
DELETE FROM profile_skills;
DELETE FROM job_details;
DELETE FROM experience;
DELETE FROM education;
DELETE FROM application_questions;
DELETE FROM Voluntary_Disclosure;
DELETE FROM university_email_otps;

-- Delete from main profile table
DELETE FROM user_profile;

-- Delete from skill master tables
DELETE FROM skill_roles;
DELETE FROM skill_groups;
DELETE FROM Core_Skills;
DELETE FROM additional_skills;
DELETE FROM programming_skills;

-- Delete from user service tables
DELETE FROM email_otp_verification;
DELETE FROM roles_category;
DELETE FROM user;

-- Note: shedlock table is kept for scheduled jobs
-- If you want to delete it too, uncomment:
-- DELETE FROM shedlock;

-- Reset auto-increment counters
ALTER TABLE profile_core_skills AUTO_INCREMENT = 1;
ALTER TABLE profile_additional_skills AUTO_INCREMENT = 1;
ALTER TABLE profile_skills AUTO_INCREMENT = 1;
ALTER TABLE job_details AUTO_INCREMENT = 1;
ALTER TABLE experience AUTO_INCREMENT = 1;
ALTER TABLE education AUTO_INCREMENT = 1;
ALTER TABLE application_questions AUTO_INCREMENT = 1;
ALTER TABLE Voluntary_Disclosure AUTO_INCREMENT = 1;
ALTER TABLE university_email_otps AUTO_INCREMENT = 1;
ALTER TABLE user_profile AUTO_INCREMENT = 1;
ALTER TABLE skill_roles AUTO_INCREMENT = 1;
ALTER TABLE skill_groups AUTO_INCREMENT = 1;
ALTER TABLE Core_Skills AUTO_INCREMENT = 1;
ALTER TABLE additional_skills AUTO_INCREMENT = 1;
ALTER TABLE programming_skills AUTO_INCREMENT = 1;
ALTER TABLE email_otp_verification AUTO_INCREMENT = 1;
ALTER TABLE roles_category AUTO_INCREMENT = 1;
ALTER TABLE user AUTO_INCREMENT = 1;

SELECT 'authd database cleared!' AS status;

-- ============================================
-- DATABASE: moodle_service_db (Moodle-Service)
-- ============================================
USE moodle_service_db;

-- Delete from dependent tables first
DELETE FROM Assessment_Attempt;
DELETE FROM moodle_course_mapping;
DELETE FROM courses;
DELETE FROM moodle_profile_users;
DELETE FROM profile_skills;
DELETE FROM skills;
DELETE FROM Requisition_Skills;
DELETE FROM requisition;
DELETE FROM employer;

-- Reset auto-increment counters
ALTER TABLE Assessment_Attempt AUTO_INCREMENT = 1;
ALTER TABLE moodle_course_mapping AUTO_INCREMENT = 1;
ALTER TABLE courses AUTO_INCREMENT = 1;
ALTER TABLE moodle_profile_users AUTO_INCREMENT = 1;
ALTER TABLE profile_skills AUTO_INCREMENT = 1;
ALTER TABLE skills AUTO_INCREMENT = 1;
ALTER TABLE Requisition_Skills AUTO_INCREMENT = 1;
ALTER TABLE requisition AUTO_INCREMENT = 1;
ALTER TABLE employer AUTO_INCREMENT = 1;

SELECT 'moodle_service_db database cleared!' AS status;

-- ============================================
-- DATABASE: Interviewer (Interviewer-Service)
-- ============================================
USE Interviewer;

-- Delete from interviewer tables
DELETE FROM feedback;
DELETE FROM interview;
DELETE FROM availability_slot;
DELETE FROM Interviewer_Details;
DELETE FROM Skill_Details;

-- Reset auto-increment counters
ALTER TABLE feedback AUTO_INCREMENT = 1;
ALTER TABLE interview AUTO_INCREMENT = 1;
ALTER TABLE availability_slot AUTO_INCREMENT = 1;
ALTER TABLE Interviewer_Details AUTO_INCREMENT = 1;
ALTER TABLE Skill_Details AUTO_INCREMENT = 1;

SELECT 'Interviewer database cleared!' AS status;

-- Re-enable foreign key checks and safe update mode
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- Final summary
SELECT '=== ALL DATABASES CLEARED ===' AS summary;
SELECT 
    'authd' AS database_name,
    (SELECT COUNT(*) FROM authd.user_profile) AS remaining_profiles,
    (SELECT COUNT(*) FROM authd.user) AS remaining_users
UNION ALL
SELECT 
    'moodle_service_db',
    (SELECT COUNT(*) FROM moodle_service_db.moodle_profile_users),
    (SELECT COUNT(*) FROM moodle_service_db.courses)
UNION ALL
SELECT 
    'Interviewer',
    (SELECT COUNT(*) FROM Interviewer.interview),
    0;

SELECT 'All data deleted successfully! Tables are preserved and ready for new data.' AS final_status;
