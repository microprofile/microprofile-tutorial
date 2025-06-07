#!/bin/bash

# Build the application
mvn clean package

# Build the Docker image
docker build -t order-service .

# Run the container
docker run -d --name order-service -p 8050:8050 -p 8051:8051 order-service
