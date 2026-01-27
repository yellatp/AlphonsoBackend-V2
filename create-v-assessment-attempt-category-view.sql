-- Drop the table if it exists and create the view instead
-- This view joins assessment_category_score, assessment_attempt, and moodle_profile_users

USE moodle_service_db;

-- 1. Drop the table if it exists (if it's currently a table)
DROP TABLE IF EXISTS v_assessment_attempt_category;

-- 2. Create the view
CREATE VIEW v_assessment_attempt_category AS
SELECT 
    acs.id AS category_score_id,
    aa.id AS attempt_id,
    aa.moodle_user_id,
    mpu.profile_id,
    mpu.email,
    aa.moodle_quiz_id,
    aa.attempt_date,
    aa.score,
    acs.category_id,
    acs.category_name,
    acs.earned,
    acs.possible,
    acs.percentage,
    acs.question_count
FROM assessment_category_score acs
INNER JOIN assessment_attempt aa ON acs.attempt_ref_id = aa.id
INNER JOIN moodle_profile_users mpu ON aa.moodle_user_id = mpu.moodle_user_id;

-- 3. Verify the view was created
SELECT COUNT(*) AS view_row_count FROM v_assessment_attempt_category;

-- Note: The view will automatically show data once assessment_attempt and 
-- assessment_category_score tables are populated by the sync job.
