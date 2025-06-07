#!/bin/bash

# Script to build and run the Payment service in Docker

# Stop execution on any error
set -e

# Navigate to the payment service directory
cd "$(dirname "$0")"

# Build the project with Maven
echo "Building with Maven..."
mvn clean package

# Build the Docker image
echo "Building Docker image..."
docker build -t payment-service .

# Run the Docker container
echo "Starting Docker container..."
docker run -d --name payment-service -p 9050:9050 payment-service

echo "Payment service is running on http://localhost:9050/payment"
