#!/bin/bash

# Build all projects

echo "Building Catalog Service..."
cd catalog && mvn clean package && cd ..

echo "Building Payment Service..."
cd payment && mvn clean package && cd ..

echo "All services are running:"
echo "- Catalog Service: http://localhost:5050/catalog"
echo "- Payment Service: http://localhost:9050/payment"