-- Fix user quiz status and enable sync
-- This script updates the user's status to QUIZ_COMPLETED so the sync job can pick them up

USE moodle_service_db;

-- 1. Check current status
SELECT 
    id,
    profile_id,
    email,
    moodle_user_id,
    status,
    created_at,
    updated_at
FROM moodle_profile_users
WHERE email = 'bablubalu624@gmail.com';

-- 2. Update status to QUIZ_COMPLETED so the sync job can process it
UPDATE moodle_profile_users
SET status = 'QUIZ_COMPLETED',
    updated_at = NOW()
WHERE email = 'bablubalu624@gmail.com'
  AND status = 'INPROGRESS';

-- 3. Verify the update
SELECT 
    id,
    profile_id,
    email,
    moodle_user_id,
    status,
    created_at,
    updated_at
FROM moodle_profile_users
WHERE email = 'bablubalu624@gmail.com';

-- Note: After running this script, the syncMoodleAttempts job (runs every 15 minutes)
-- will pick up this user and sync their quiz attempts from Moodle.
-- The job will then update the status to QUIZ_PASSED or QUIZ_FAILED based on the score.
