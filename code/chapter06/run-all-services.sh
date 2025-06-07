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
echo "- User Service: https://scaling-pancake-77vj4pwq7fpjqx-6050.app.github.dev/user"
echo "- Inventory Service: https://scaling-pancake-77vj4pwq7fpjqx-7050.app.github.dev/inventory"
echo "- Order Service: https://scaling-pancake-77vj4pwq7fpjqx-8050.app.github.dev/order"
echo "- Catalog Service: https://scaling-pancake-77vj4pwq7fpjqx-5050.app.github.dev/catalog"
echo "- Payment Service: https://scaling-pancake-77vj4pwq7fpjqx-9050.app.github.dev/payment"
echo "- Shopping Cart Service: https://scaling-pancake-77vj4pwq7fpjqx-4050.app.github.dev/shoppingcart"
echo "- Shipment Service: https://scaling-pancake-77vj4pwq7fpjqx-8060.app.github.dev/shipment"
