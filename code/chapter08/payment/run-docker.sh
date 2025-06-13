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

# Dynamically determine the service URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    # GitHub Codespaces environment
    SERVICE_URL="https://$CODESPACE_NAME-9050.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN/payment"
    echo "Payment service is running in GitHub Codespaces"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    # Gitpod environment
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    SERVICE_URL="https://9050-$GITPOD_HOST/payment"
    echo "Payment service is running in Gitpod"
else
    # Local or other environment
    HOSTNAME=$(hostname)
    SERVICE_URL="http://$HOSTNAME:9050/payment"
    echo "Payment service is running locally"
fi

echo "Service URL: $SERVICE_URL"
echo ""
echo "Available endpoints:"
echo "  - Health check: $SERVICE_URL/health"
echo "  - API documentation: $SERVICE_URL/openapi"
echo "  - Configuration: $SERVICE_URL/api/payment-config"
