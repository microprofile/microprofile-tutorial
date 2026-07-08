#!/bin/bash

# Build all projects
echo "Building User Service..."
cd user && mvn clean package && cd ..

echo "Building Inventory Service..."
cd inventory && mvn clean package && cd ..

echo "Building Order Service..."
cd order && mvn clean package && cd ..

echo "Building Catalog Service..."
cd catalog && mvn clean package && cd ..

echo "Building Payment Service..."
cd payment && mvn clean package && cd ..

echo "Building Shopping Cart Service..."
cd shoppingcart && mvn clean package && cd ..

echo "Building Shipment Service..."
cd shipment && mvn clean package && cd ..

# Start all services using docker-compose
echo "Starting all services with Docker Compose..."
docker-compose up -d

echo "All services are running:"
echo "- User Service: https://<hostname>/user"
echo "- Inventory Service: https://<hostname>/inventory"
echo "- Order Service: https://<hostname>/order"
echo "- Catalog Service: https://<hostname>/catalog"
echo "- Payment Service: https://<hostname>/payment"
echo "- Shopping Cart Service: https://<hostname>/shoppingcart"
echo "- Shipment Service: https://<hostname>/shipment"