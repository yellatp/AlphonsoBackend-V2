#!/bin/bash

# Start script for AlphonsoBackend-V2 microservices
# Make sure MySQL is running before executing this script

echo "Starting AlphonsoBackend-V2 Services..."
echo "========================================"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to start a service
start_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    
    echo -e "${YELLOW}Starting $service_name on port $port...${NC}"
    cd "$service_path"
    mvn spring-boot:run > "../logs/${service_name}.log" 2>&1 &
    echo $! > "../logs/${service_name}.pid"
    echo -e "${GREEN}$service_name started (PID: $(cat ../logs/${service_name}.pid))${NC}"
    cd ..
    sleep 5  # Wait a bit before starting next service
}

# Create logs directory
mkdir -p logs

# 1. Start Service-Registry (Eureka) - Must start first
echo -e "\n${YELLOW}Step 1: Starting Service-Registry (Eureka)...${NC}"
start_service "Service-Registry" "Service-Registery" "8761"

# Wait for Eureka to be ready
echo "Waiting for Eureka to be ready..."
sleep 15

# 2. Start User-Service
echo -e "\n${YELLOW}Step 2: Starting User-Service...${NC}"
start_service "User-Service" "User-Service" "8080"

# 3. Start Profile-Service
echo -e "\n${YELLOW}Step 3: Starting Profile-Service...${NC}"
start_service "Profile-Service" "Profile-Service" "8081"

# 4. Start Moodle&EmployerService
echo -e "\n${YELLOW}Step 4: Starting Moodle&EmployerService...${NC}"
start_service "Moodle-Service" "Moodle&EmployerService" "8082"

# 5. Start Interviewer-Service
echo -e "\n${YELLOW}Step 5: Starting Interviewer-Service...${NC}"
start_service "Interviewer-Service" "Interviewer-Service" "8083"

# Wait for services to register with Eureka
echo "Waiting for services to register with Eureka..."
sleep 10

# 6. Start Api-Gateway (should start last)
echo -e "\n${YELLOW}Step 6: Starting Api-Gateway...${NC}"
start_service "Api-Gateway" "Api-Gateway" "9191"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services started!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Service URLs:"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - Api-Gateway: http://localhost:9191"
echo "  - User-Service: http://localhost:8080"
echo "  - Profile-Service: http://localhost:8081"
echo "  - Moodle-Service: http://localhost:8082"
echo "  - Interviewer-Service: http://localhost:8083"
echo ""
echo "Logs are available in the 'logs' directory"
echo "To stop services, run: ./stop-services.sh"
