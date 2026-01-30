# Docker Setup for AlphonsoBackend-V2

This guide explains how to run all backend microservices using Docker Compose.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose v3.8+
- At least 4GB RAM available for Docker

## Quick Start

### Build and Run All Services

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode (background)
docker-compose up -d --build

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f api-gateway

# Stop all services
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

## Services

Once started, the following services will be available:

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:9191
- **User Service**: http://localhost:8080
- **Profile Service**: http://localhost:8081
- **Moodle Service**: http://localhost:8082
- **Interviewer Service**: http://localhost:8083
- **MySQL Database**: localhost:3306

## Service Startup Order

The services start in the following order:

1. MySQL Database
2. Service Registry (Eureka)
3. User Service
4. Profile Service
5. Moodle Service
6. Interviewer Service
7. API Gateway (starts last)

## Database Configuration

The MySQL container automatically creates the required databases:
- `authd` (for User-Service)
- `moodle_service_db` (for Profile-Service and Moodle-Service)
- `Interviewer` (for Interviewer-Service)

Default credentials:
- **Root Password**: `rootpassword`
- **User**: `alphonso`
- **Password**: `alphonso123`

## Environment Variables

You can customize database and service configuration in `docker-compose.yml`:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/dbname
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: rootpassword
  EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://service-registry:8761/eureka
```

## Health Checks

All services include health checks. Check service status:

```bash
# Check all services
docker-compose ps

# Check specific service health
docker inspect alphonso-api-gateway | grep Health -A 10
```

## Troubleshooting

### Port Conflicts

If ports are already in use, modify port mappings in `docker-compose.yml`:

```yaml
ports:
  - "9192:9191"  # Change host port
```

### Service Won't Start

1. Check logs:
   ```bash
   docker-compose logs service-name
   ```

2. Ensure MySQL is healthy:
   ```bash
   docker-compose ps mysql
   ```

3. Check Eureka registration:
   - Visit http://localhost:8761
   - Verify all services are registered

### Database Connection Issues

1. Verify MySQL is running:
   ```bash
   docker-compose ps mysql
   ```

2. Check database exists:
   ```bash
   docker-compose exec mysql mysql -uroot -prootpassword -e "SHOW DATABASES;"
   ```

3. Restart services:
   ```bash
   docker-compose restart
   ```

### Build Failures

Clear Docker cache and rebuild:

```bash
docker-compose build --no-cache
docker-compose up
```

### Out of Memory

If services fail due to memory issues:

1. Increase Docker Desktop memory limit (Settings → Resources → Memory)
2. Reduce concurrent builds:
   ```bash
   docker-compose build --parallel 1
   ```

## Development Mode

For development, you can still use the shell scripts:

```bash
# Start services locally (requires local MySQL)
./start-services.sh

# Stop services
./stop-services.sh
```

## Production Considerations

For production deployment:

1. Change default passwords in `docker-compose.yml`
2. Use environment files (`.env`) for sensitive data
3. Configure proper networking and security
4. Set up volume backups for MySQL data
5. Use Docker secrets for sensitive information
6. Configure resource limits for each service

## Monitoring

### View Service Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway

# Last 100 lines
docker-compose logs --tail=100 api-gateway
```

### Check Service Status

```bash
# List all containers
docker-compose ps

# Check resource usage
docker stats
```
