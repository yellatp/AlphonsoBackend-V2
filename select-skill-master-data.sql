-- Script to select/query skill data for a PARTICULAR USER from authd database
-- 
-- USAGE: Replace 'USER_EMAIL_HERE' with the actual user email before running
-- Example: SET @user_email = 'bablubalu624@gmail.com';

USE authd;

-- Set the user email here (change this to the email you want to query)
SET @user_email = 'bablubalu624@gmail.com';

-- Get the profile ID for this user
SET @profile_id = (SELECT id FROM user_profile WHERE email = @user_email);

-- User profile information
SELECT '=== USER PROFILE ===' AS section;
SELECT id, profile_id, email, first_name, last_name, status, assessment_status
FROM user_profile 
WHERE email = @user_email;

-- Profile Skills for this user
SELECT '=== PROFILE SKILLS FOR USER ===' AS section;
SELECT 
    ps.id AS profile_skill_id,
    ps.profile_id,
    up.email,
    up.first_name,
    up.last_name,
    sg.id AS group_id,
    sg.group_name,
    sr.id AS role_id,
    sr.role_name,
    ps.programming_skill_id,
    prog.program_name AS programming_skill_name
FROM profile_skills ps
INNER JOIN user_profile up ON ps.profile_id = up.id
LEFT JOIN skill_groups sg ON ps.group_id = sg.id
LEFT JOIN skill_roles sr ON ps.role_id = sr.id
LEFT JOIN programming_skills prog ON ps.programming_skill_id = prog.id
WHERE up.email = @user_email;

-- Core Skills for this user
SELECT '=== CORE SKILLS FOR USER ===' AS section;
SELECT 
    cs.id AS core_skill_id,
    cs.skill_name,
    up.email,
    up.first_name,
    up.last_name
FROM Core_Skills cs
INNER JOIN profile_core_skills pcs ON cs.id = pcs.core_skill_id
INNER JOIN profile_skills ps ON pcs.profile_skill_id = ps.id
INNER JOIN user_profile up ON ps.profile_id = up.id
WHERE up.email = @user_email
ORDER BY cs.skill_name;

-- Additional Skills for this user
SELECT '=== ADDITIONAL SKILLS FOR USER ===' AS section;
SELECT 
    as.id AS additional_skill_id,
    as.skill_name,
    up.email,
    up.first_name,
    up.last_name
FROM additional_skills as
INNER JOIN profile_additional_skills pas ON as.id = pas.additional_skill_id
INNER JOIN profile_skills ps ON pas.profile_skill_id = ps.id
INNER JOIN user_profile up ON ps.profile_id = up.id
WHERE up.email = @user_email
ORDER BY as.skill_name;

-- Summary for this user
SELECT '=== SUMMARY FOR USER ===' AS section;
SELECT 
    up.email,
    up.first_name,
    up.last_name,
    COUNT(DISTINCT ps.id) AS profile_skills_count,
    COUNT(DISTINCT pcs.core_skill_id) AS core_skills_count,
    COUNT(DISTINCT pas.additional_skill_id) AS additional_skills_count,
    COUNT(DISTINCT ps.programming_skill_id) AS programming_skills_count
FROM user_profile up
LEFT JOIN profile_skills ps ON up.id = ps.profile_id
LEFT JOIN profile_core_skills pcs ON ps.id = pcs.profile_skill_id
LEFT JOIN profile_additional_skills pas ON ps.id = pas.profile_skill_id
WHERE up.email = @user_email
GROUP BY up.id, up.email, up.first_name, up.last_name;

-- If no skills found, show message
SELECT 
    CASE 
        WHEN NOT EXISTS (
            SELECT 1 FROM profile_skills ps 
            WHERE ps.profile_id = @profile_id
        ) 
        THEN CONCAT('No skills data found for user: ', @user_email)
        ELSE CONCAT('Skills data retrieved for user: ', @user_email)
    END AS status;
