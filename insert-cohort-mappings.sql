-- Insert cohort mappings into moodle_course_mapping table
-- Note: id column is auto-generated, so we don't include it in the INSERT

USE moodle_service_db;

INSERT INTO moodle_course_mapping
(moodle_course_id, skill_name, moodle_course_name, cohort_id, cohort_idnumber, active, processed, created_at, last_synced)
VALUES
(2,  'Java Fundamentals',        'Java Fundamentals',        1,  'CH_2',  1, 1, '2025-11-10 17:23:52.160031', '2025-11-10 17:23:51.780077'),
(3,  'Spring Boot Advanced',      'Spring Boot Advanced',     2,  'CH_3',  1, 1, '2025-11-10 17:24:27.693696', '2025-11-10 17:24:27.693696'),
(4,  'Devops Introduction',       'Devops Introduction',      3,  'CH_4',  1, 1, '2025-11-10 17:24:30.961747', '2025-11-10 17:24:30.961747'),
(9,  'Flask Introduction',       'Flask Introduction',       4,  'CH_9',  1, 1, '2025-11-10 21:26:05.871039', '2025-11-10 21:26:05.871039'),
(10, 'JavaScript Fundamentals',   'JavaScript Fundamentals',  5,  'CH_10', 1, 1, '2025-11-10 21:26:06.483334', '2025-11-10 21:26:06.483334'),
(11, 'React Development',         'React Development',        6,  'CH_11', 1, 1, '2025-11-10 21:26:07.021193', '2025-11-10 21:26:07.021193'),
(12, 'Angular Advanced',          'Angular Advanced',          7,  'CH_12', 1, 1, '2025-11-10 21:26:07.556605', '2025-11-10 21:26:07.556605'),
(13, 'Node.js Fundamentals',      'Node.js Fundamentals',     8,  'CH_13', 1, 1, '2025-11-10 21:26:08.087807', '2025-11-10 21:26:08.087807'),
(14, 'Docker Introduction',       'Docker Introduction',      9,  'CH_14', 1, 1, '2025-11-10 21:26:08.619889', '2025-11-10 21:26:08.619889'),
(15, 'Kubernetes Advanced',        'Kubernetes Advanced',      10, 'CH_15', 1, 1, '2025-11-10 21:26:09.187177', '2025-11-10 21:26:09.187177'),
(16, 'AWS Cloud Essentials',      'AWS Cloud Essentials',      11, 'CH_16', 1, 1, '2025-11-10 21:26:09.727707', '2025-11-10 21:26:09.727707'),
(17, 'Azure Cloud Introduction',  'Azure Cloud Introduction', 12, 'CH_17', 1, 1, '2025-11-10 21:26:10.346514', '2025-11-10 21:26:10.346514'),
(5,  'Hibernate Advanced',        'Hibernate Advanced',        13, 'CH_5',  1, 1, '2025-11-10 21:26:10.918843', '2025-11-10 21:26:10.918843'),
(6,  'Selenium Automation',       'Selenium Automation',       14, 'CH_6',  1, 1, '2025-11-10 21:26:11.456203', '2025-11-10 21:26:11.456203'),
(7,  'Python Fundamentals',       'Python Fundamentals',       15, 'CH_7',  1, 1, '2025-11-10 21:26:12.022453', '2025-11-10 21:26:12.022453'),
(8,  'Django Advanced',           'Django Advanced',           16, 'CH_8',  1, 1, '2025-11-10 21:26:12.553966', '2025-11-10 21:26:12.553966'),
(18, 'Assessment Host Course',    'Assessment Host Course',    17, 'CH_18', 1, 1, '2025-11-11 19:46:05.915961', '2025-11-11 19:46:05.915961'),
(19, 'TestingAlphonso',          'TestingAlphonso',           18, 'CH_19', 1, 1, '2026-01-26 18:38:12.410034', '2026-01-26 18:38:12.127661');
