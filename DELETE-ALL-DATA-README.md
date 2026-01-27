# Delete All Data Script

This script deletes **ALL data** from **ALL databases** while preserving table structures.

## ⚠️ WARNING

**This script will permanently delete all data from:**
- `authd` database (User-Service & Profile-Service)
- `moodle_service_db` database (Moodle-Service)
- `Interviewer` database (Interviewer-Service)

**The script:**
- ✅ Preserves table structures (tables are NOT dropped)
- ✅ Resets auto-increment counters
- ✅ Handles foreign key constraints
- ❌ **DELETES ALL DATA** - This cannot be undone!

## Usage

### Option 1: Command Line

```bash
mysql -u root -p < delete-all-data.sql
```

### Option 2: MySQL Client

```bash
mysql -u root -p
```

Then:
```sql
SOURCE /Users/srinivas/Documents/chanduAnna/AlphonsoBackend-V2/delete-all-data.sql;
```

### Option 3: Copy and Paste

1. Open `delete-all-data.sql`
2. Copy the entire script
3. Paste and run in your MySQL client

## What Gets Deleted

### authd Database
- All user profiles (`user_profile`)
- All user registrations (`users_reg`)
- All profile skills (`profile_skills`)
- All skill master data (groups, roles, core skills, additional skills, programming skills)
- All education, experience, job details
- All application questions
- All voluntary disclosures
- All university email OTPs
- All OTPs
- **Note:** `shedlock` table is preserved (needed for scheduled jobs)

### moodle_service_db Database
- All Moodle profiles (`moodle_profile_users`)
- All courses (`courses`)
- All course mappings (`moodle_course_mapping`)
- All assessment attempts (`assessment_attempts`)

### Interviewer Database
- All interviews (`interviews`)
- All skill details (`Skill_Details`)

## After Running

After running this script:
1. All tables will be empty but preserved
2. Auto-increment counters will be reset to 1
3. You can start fresh with new data
4. The application will recreate data as users interact with it

## Verification

The script includes verification queries at the end to show:
- Count of remaining records in each database
- Confirmation that data was deleted

## Recreating Data

After deletion, data will be recreated when:
- Users register/login
- Users fill out profiles
- Admin creates skill master data
- Scheduled jobs run
- API calls create new records

## Notes

- The script uses `SET FOREIGN_KEY_CHECKS = 0` to handle foreign key constraints
- Tables are NOT dropped, only data is deleted
- If you need to also drop tables, you'll need a separate script
- The `shedlock` table is preserved to avoid issues with scheduled jobs
