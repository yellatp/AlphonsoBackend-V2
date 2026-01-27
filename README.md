# AlphonsoBackend-V2

## Important Setup Notes

### CORS Configuration (Required for Frontend Integration)

**⚠️ IMPORTANT:** The API Gateway includes CORS configuration to allow frontend requests. This was added to fix CORS errors when making requests from the frontend (e.g., `http://localhost:3000`) to the API Gateway.

**Location:** `Api-Gateway/src/main/java/com/alphonso/api_gateway/config/CorsConfig.java`

**What it does:**
- Allows requests from frontend origins: `http://localhost:3000`, `http://localhost:3001`, `http://localhost:5173`
- Handles preflight OPTIONS requests
- Allows credentials (cookies, authorization headers)

**If you encounter CORS errors:**
1. Make sure the API Gateway has been rebuilt after adding the CORS config
2. Restart the API Gateway service
3. Verify the `CorsConfig.java` file exists in the Api-Gateway project

### Shedlock Table (Required for Scheduled Jobs)

**⚠️ IMPORTANT:** The Profile-Service uses ShedLock for distributed locking of scheduled tasks. The `shedlock` table must be created in the `authd` database.

**To create the table:**
```bash
mysql -u root -p authd < create-shedlock-table.sql
```

Or manually:
```sql
USE authd;
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

**Location:** `create-shedlock-table.sql` in the root directory

**Why it's needed:**
- Prevents duplicate execution of scheduled tasks across multiple service instances
- Required for `ProfileSyncJob` scheduled tasks to work properly
- Without this table, you'll see errors like: `Table 'authd.shedlock' doesn't exist`
