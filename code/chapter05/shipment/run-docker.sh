#!/bin/bash

# Build and run the Shipment Service in Docker
echo "Building and starting Shipment Service in Docker..."

# Build the application
mvn clean package

# Build and run the Docker image
docker build -t shipment-service .
docker run -p 8060:8060 -p 9060:9060 --name shipment-service shipment-service
