#!/bin/bash

# Navigate to the order service directory
cd "$(dirname "$0")"

# Build the project
echo "Building Order Service..."
mvn clean package

# Run the Liberty server
echo "Starting Order Service..."
mvn liberty:run
