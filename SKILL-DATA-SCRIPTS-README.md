# Skill Data Management Scripts

These scripts allow you to delete and query skill data for a **particular user** in the `authd` database.

## Files

1. **`delete-skill-master-data.sql`** - Deletes skill data for a specific user
2. **`select-skill-master-data.sql`** - Queries/selects skill data for a specific user

## Usage

### Step 1: Edit the Script

Before running either script, you need to set the user email:

1. Open the SQL script file
2. Find this line: `SET @user_email = 'USER_EMAIL_HERE';`
3. Replace `'USER_EMAIL_HERE'` with the actual user email
4. Example: `SET @user_email = 'bablubalu624@gmail.com';`

### Step 2: Run the Script

#### Option A: MySQL Command Line

```bash
# Delete skill data for a user
mysql -u root -p authd < delete-skill-master-data.sql

# Select/query skill data for a user
mysql -u root -p authd < select-skill-master-data.sql
```

#### Option B: MySQL Client

```bash
# Connect to MySQL
mysql -u root -p

# Use the database
USE authd;

# Run the script
SOURCE /Users/srinivas/Documents/chanduAnna/AlphonsoBackend-V2/delete-skill-master-data.sql;
# or
SOURCE /Users/srinivas/Documents/chanduAnna/AlphonsoBackend-V2/select-skill-master-data.sql;
```

#### Option C: Copy and Paste

1. Open the SQL file
2. Edit the email address
3. Copy the entire script
4. Paste and run in your MySQL client (MySQL Workbench, DBeaver, etc.)

## What Gets Deleted (delete script)

For the specified user:
- ✅ Junction table entries (`profile_core_skills`, `profile_additional_skills`)
- ✅ Profile skills record (`profile_skills`)
- ❌ **NOT deleted**: Master skill tables (skill_groups, skill_roles, Core_Skills, etc.) - these are shared across all users

## What Gets Selected (select script)

For the specified user:
- User profile information
- Profile skills (group, role, programming skill)
- Core skills list
- Additional skills list
- Summary counts

## Example

To delete skills for user `bablubalu624@gmail.com`:

1. Edit `delete-skill-master-data.sql`:
   ```sql
   SET @user_email = 'bablubalu624@gmail.com';
   ```

2. Run the script:
   ```bash
   mysql -u root -p authd < delete-skill-master-data.sql
   ```

3. Verify deletion by running the select script:
   ```sql
   SET @user_email = 'bablubalu624@gmail.com';
   ```
   Then run `select-skill-master-data.sql`

## Notes

- The scripts preserve all master skill data (skill_groups, skill_roles, Core_Skills, etc.)
- Only user-specific skill associations are deleted
- After deletion, the user can re-select skills through the application
- The scripts include verification queries to confirm the operations
