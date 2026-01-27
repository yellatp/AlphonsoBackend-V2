#!/bin/bash

# Stop script for AlphonsoBackend-V2 microservices

echo "Stopping AlphonsoBackend-V2 Services..."
echo "========================================"

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}Stopping $service_name (PID: $pid)...${NC}"
            kill "$pid"
            rm "$pid_file"
            echo -e "${RED}$service_name stopped${NC}"
        else
            echo -e "${YELLOW}$service_name was not running${NC}"
            rm "$pid_file"
        fi
    else
        echo -e "${YELLOW}$service_name PID file not found${NC}"
    fi
}

# Stop services in reverse order
stop_service "Api-Gateway"
stop_service "Interviewer-Service"
stop_service "Moodle-Service"
stop_service "Profile-Service"
stop_service "User-Service"
stop_service "Service-Registry"

echo -e "\n${RED}All services stopped!${NC}"
