# Fix for Services Not Registering with Eureka

## Issues Found

1. **Password Mismatch**: Profile-Service, Moodle-Service, and Interviewer-Service were using password `Mahadesh#99`, but your MySQL root password is `Root@123` (same as User-Service which is working).

2. **Services Need Restart**: After fixing the password, the services need to be restarted to pick up the new configuration.

## Fixes Applied

✅ Updated password in:
- `Profile-Service/src/main/resources/application-dev.yml` → `Root@123`
- `Moodle&EmployerService/src/main/resources/application-dev.yml` → `Root@123`
- `Interviewer-Service/src/main/resources/application-dev.yml` → `Root@123`

## How to Restart Services

### Option 1: Stop and Restart Manually

1. **Stop the failing services** (if they're still running):
   ```bash
   # Find and kill the processes
   pkill -f "Profile-Service.*spring-boot"
   pkill -f "Moodle.*spring-boot"
   pkill -f "Interviewer-Service.*spring-boot"
   ```

2. **Restart each service** in separate terminals:
   ```bash
   # Terminal 1 - Profile-Service
   cd Profile-Service
   mvn spring-boot:run
   
   # Terminal 2 - Moodle-Service
   cd Moodle&EmployerService
   mvn spring-boot:run
   
   # Terminal 3 - Interviewer-Service
   cd Interviewer-Service
   mvn spring-boot:run
   ```

### Option 2: Use the Start Script

Simply run the start script again (it will restart services with the new config):
```bash
./start-services.sh
```

## Verify Services are Registered

After restarting, check Eureka dashboard:
- Open: http://localhost:8761/
- You should see all services registered:
  - ✅ api-gateway
  - ✅ User-Service
  - ✅ Profile-Service (after restart)
  - ✅ moodle-service (after restart)
  - ✅ Interviewer-Service (after restart)

## Expected Behavior

Once all services are running with the correct password:
1. They will connect to MySQL successfully
2. Databases will be created automatically (if not exists)
3. Tables will be created automatically from JPA entities
4. Services will register with Eureka
5. All services will appear in the Eureka dashboard

## Troubleshooting

If services still don't register:
1. Check logs in the `logs/` directory
2. Verify MySQL is running: `mysql -u root -p -e "SELECT 1;"`
3. Verify password is correct: Try connecting manually with `Root@123`
4. Check Eureka is running: http://localhost:8761/
