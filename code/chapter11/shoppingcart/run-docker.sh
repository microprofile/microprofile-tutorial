#!/bin/bash

# Script to build and run the Shopping Cart service in Docker

# Stop execution on any error
set -e

# Navigate to the shopping cart service directory
cd "$(dirname "$0")"

# Build the project with Maven
echo "Building with Maven..."
mvn clean package

# Build the Docker image
echo "Building Docker image..."
docker build -t shoppingcart-service .

# Run the Docker container
echo "Starting Docker container..."
docker run -d --name shoppingcart-service -p 4050:4050 shoppingcart-service

echo "Shopping Cart service is running on http://localhost:4050/shoppingcart"
