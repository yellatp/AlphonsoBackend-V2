#!/bin/bash

# Docker startup script for AlphonsoBackend-V2
# This script builds and starts all services

echo "=========================================="
echo "AlphonsoBackend-V2 Docker Setup"
echo "=========================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Build and start services
echo "Building and starting all services..."
echo "This may take several minutes on first run..."
echo ""

docker-compose up --build -d

echo ""
echo "Waiting for services to start..."
sleep 10

# Check service status
echo ""
echo "=========================================="
echo "Service Status:"
echo "=========================================="
docker-compose ps

echo ""
echo "=========================================="
echo "Service URLs:"
echo "=========================================="
echo "  📊 Eureka Dashboard: http://localhost:8761"
echo "  🌐 API Gateway:      http://localhost:9191"
echo "  👤 User Service:     http://localhost:8080"
echo "  📝 Profile Service:  http://localhost:8081"
echo "  🎓 Moodle Service:   http://localhost:8082"
echo "  💼 Interviewer Service: http://localhost:8083"
echo "  🗄️  MySQL Database:   localhost:3306"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop:      docker-compose down"
echo ""
