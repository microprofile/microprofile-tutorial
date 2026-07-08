#!/bin/bash

# Script to build and run the Payment service

# Stop execution on any error
set -e

echo "Building and running Payment service..."

# Navigate to the payment service directory
cd "$(dirname "$0")"

# Build the project with Maven
echo "Building with Maven..."
mvn clean package

# Run the application using Liberty Maven plugin
echo "Starting Liberty server..."
mvn liberty:run
