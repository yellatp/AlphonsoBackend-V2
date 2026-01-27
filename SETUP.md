# AlphonsoBackend-V2 Setup Guide

## Prerequisites

1. **Java 17+** - Required for Spring Boot applications
2. **Maven 3.6+** - For building and running services
3. **MySQL 8.0+** - Database server
4. **MySQL running** - Make sure MySQL service is running

## Database Setup

The services are configured to automatically create databases if they don't exist (`createDatabaseIfNotExist=true`). However, you can also create them manually:

### Option 1: Automatic Creation (Recommended)
The databases will be created automatically when services start, as long as MySQL is running.

### Option 2: Manual Creation
If you prefer to create databases manually, run:

```bash
mysql -u root -p < create-databases.sql
```

Or connect to MySQL and run:

```sql
CREATE DATABASE IF NOT EXISTS authd;
CREATE DATABASE IF NOT EXISTS moodle_service_db;
CREATE DATABASE IF NOT EXISTS Interviewer;
```

### Required Database Tables

#### Shedlock Table (Required for Scheduled Jobs)

**⚠️ IMPORTANT:** The Profile-Service requires the `shedlock` table in the `authd` database for distributed locking of scheduled tasks. This table is NOT automatically created.

**Create the table:**
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

**Why it's needed:**
- Prevents duplicate execution of scheduled tasks across multiple service instances
- Required for `ProfileSyncJob` scheduled tasks to work properly
- Without this table, you'll see errors: `Table 'authd.shedlock' doesn't exist`

**Location:** `create-shedlock-table.sql` in the root directory

### Database Configuration
- **Host**: localhost:3306
- **Username**: root
- **Password**: Mahadesh#99
- **Databases**:
  - `authd` - Used by User-Service and Profile-Service
  - `moodle_service_db` - Used by Moodle&EmployerService
  - `Interviewer` - Used by Interviewer-Service

## Starting MySQL

### On macOS (Homebrew):
```bash
brew services start mysql
```

### On Linux:
```bash
sudo systemctl start mysql
# or
sudo service mysql start
```

### Verify MySQL is running:
```bash
mysql -u root -p -e "SELECT 1;"
```

## Starting the Services

Services must be started in the following order:

### 1. Start Service-Registry (Eureka) - Port 8761
```bash
cd Service-Registery
mvn spring-boot:run
```

Wait for Eureka to fully start (you'll see "Started ServiceRegisteryApplication" in the logs).

### 2. Start User-Service - Port 8080
```bash
cd User-Service
mvn spring-boot:run
```

### 3. Start Profile-Service - Port 8081
```bash
cd Profile-Service
mvn spring-boot:run
```

### 4. Start Moodle&EmployerService - Port 8082
```bash
cd Moodle&EmployerService
mvn spring-boot:run
```

### 5. Start Interviewer-Service - Port 8083
```bash
cd Interviewer-Service
mvn spring-boot:run
```

### 6. Start Api-Gateway - Port 9191 (Start last)
```bash
cd Api-Gateway
mvn spring-boot:run
```

## Using the Start Script

Alternatively, you can use the provided script (requires MySQL to be running):

```bash
./start-services.sh
```

This will start all services in the background. Logs will be available in the `logs/` directory.

To stop all services:
```bash
./stop-services.sh
```

## Service URLs

Once all services are running:

- **Eureka Dashboard**: http://localhost:8761
- **Api-Gateway**: http://localhost:9191
- **User-Service**: http://localhost:8080
- **Profile-Service**: http://localhost:8081
- **Moodle-Service**: http://localhost:8082
- **Interviewer-Service**: http://localhost:8083

## API Gateway Routes

All API requests should go through the API Gateway:

- User Service: `http://localhost:9191/api/user/**`
- Profile Service: `http://localhost:9191/api/profile/**`
- Moodle Service: `http://localhost:9191/api/moodle/**`
- Interviewer Service: `http://localhost:9191/api/interviewer/**`

## CORS Configuration

**⚠️ IMPORTANT:** The API Gateway includes CORS configuration to allow frontend requests. This is required for the frontend to communicate with the backend.

**Location:** `Api-Gateway/src/main/java/com/alphonso/api_gateway/config/CorsConfig.java`

**What it does:**
- Allows requests from frontend origins: `http://localhost:3000`, `http://localhost:3001`, `http://localhost:3003`, `http://localhost:5173`
- Handles preflight OPTIONS requests
- Allows credentials (cookies, authorization headers)

**If you encounter CORS errors:**
1. Make sure the API Gateway has been rebuilt: `cd Api-Gateway && mvn clean install`
2. Restart the API Gateway service
3. Verify the `CorsConfig.java` file exists in the Api-Gateway project
4. Check browser console for specific CORS error messages

**Common CORS errors:**
- `Access to XMLHttpRequest has been blocked by CORS policy` - API Gateway CORS config not loaded
- `No 'Access-Control-Allow-Origin' header is present` - API Gateway not running or CORS config missing

## Troubleshooting

### Services won't start
1. Check if MySQL is running: `mysql -u root -p -e "SELECT 1;"`
2. Check if ports are available: `lsof -i :8761` (replace with the port number)
3. Check logs in the `logs/` directory

### Database connection errors
1. Verify MySQL is running
2. Check database credentials in `application-dev.yml` files
3. Ensure databases exist or can be created
4. **Check if shedlock table exists** (see Required Database Tables section above)

### CORS errors from frontend
1. Verify API Gateway is running on port 9191
2. Check that `CorsConfig.java` exists and has been compiled
3. Rebuild API Gateway: `cd Api-Gateway && mvn clean install`
4. Restart API Gateway service
5. Check browser console for specific error messages

### Maven build errors
1. Clean and rebuild: `mvn clean install`
2. Update dependencies: `mvn dependency:resolve`
3. Check Java version: `java -version` (should be 17+)

### Scheduled job errors
1. Check if `shedlock` table exists in `authd` database
2. Create the table using `create-shedlock-table.sql` if missing
3. Check Profile-Service logs for ShedLock-related errors

## Development Profile

All services use the `dev` profile by default. Configuration files:
- `application.yml` - Base configuration
- `application-dev.yml` - Development-specific configuration

## Notes

- Services use JPA with `ddl-auto: update`, so database schemas will be automatically updated
- All services register with Eureka for service discovery
- JWT authentication is configured across services
- Email service is configured for OTP functionality
- **CORS is configured in API Gateway** - Required for frontend integration
- **Shedlock table must be created manually** - Not auto-created by JPA