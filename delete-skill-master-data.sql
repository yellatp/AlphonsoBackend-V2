-- Script to delete skill data for a PARTICULAR USER from authd database
-- This script handles foreign key constraints by deleting in the correct order
-- 
-- USAGE: Replace 'USER_EMAIL_HERE' with the actual user email before running
-- Example: SET @user_email = 'bablubalu624@gmail.com';

USE authd;

-- Set the user email here (change this to the email you want to delete skills for)
SET @user_email = 'bablubalu624@gmail.com';

-- Verify the user exists
SELECT '=== VERIFYING USER ===' AS step;
SELECT id, profile_id, email, first_name, last_name 
FROM user_profile 
WHERE email = @user_email;

-- Get the profile ID for this user
SET @profile_id = (SELECT id FROM user_profile WHERE email = @user_email);

-- Check if user has skills data
SELECT '=== CHECKING EXISTING SKILLS ===' AS step;
SELECT 
    ps.id AS profile_skill_id,
    ps.profile_id,
    up.email,
    sg.group_name,
    sr.role_name
FROM profile_skills ps
LEFT JOIN user_profile up ON ps.profile_id = up.id
LEFT JOIN skill_groups sg ON ps.group_id = sg.id
LEFT JOIN skill_roles sr ON ps.role_id = sr.id
WHERE up.email = @user_email;

-- Step 1: Delete from junction tables (profile_core_skills, profile_additional_skills)
-- These tables link profiles to skills
SELECT '=== DELETING JUNCTION TABLE DATA ===' AS step;
DELETE pcs FROM profile_core_skills pcs
INNER JOIN profile_skills ps ON pcs.profile_skill_id = ps.id
WHERE ps.profile_id = @profile_id;

DELETE pas FROM profile_additional_skills pas
INNER JOIN profile_skills ps ON pas.profile_skill_id = ps.id
WHERE ps.profile_id = @profile_id;

SELECT CONCAT('Deleted junction table entries for user: ', @user_email) AS status;

-- Step 2: Delete from profile_skills table for this user
SELECT '=== DELETING PROFILE SKILLS ===' AS step;
DELETE FROM profile_skills 
WHERE profile_id = @profile_id;

SELECT CONCAT('Deleted profile_skills for user: ', @user_email) AS status;

-- Verify deletion
SELECT '=== VERIFICATION ===' AS step;
SELECT 
    'profile_skills' AS table_name, 
    COUNT(*) AS remaining_count 
FROM profile_skills ps
INNER JOIN user_profile up ON ps.profile_id = up.id
WHERE up.email = @user_email
UNION ALL
SELECT 
    'profile_core_skills', 
    COUNT(*) 
FROM profile_core_skills pcs
INNER JOIN profile_skills ps ON pcs.profile_skill_id = ps.id
INNER JOIN user_profile up ON ps.profile_id = up.id
WHERE up.email = @user_email
UNION ALL
SELECT 
    'profile_additional_skills', 
    COUNT(*) 
FROM profile_additional_skills pas
INNER JOIN profile_skills ps ON pas.profile_skill_id = ps.id
INNER JOIN user_profile up ON ps.profile_id = up.id
WHERE up.email = @user_email;

SELECT CONCAT('Skill data deleted successfully for user: ', @user_email) AS status;
