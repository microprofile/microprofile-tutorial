#!/bin/bash
# Script to restart the Liberty server and test the Swagger UI

# Change to the order directory
cd /workspaces/liberty-rest-app/order

# Build the application
echo "Building the Order Service application..."
mvn clean package

# Stop the Liberty server
echo "Stopping Liberty server..."
mvn liberty:stop

# Copy the public key to the Liberty server config directory
echo "Copying public key to Liberty config directory..."
mkdir -p target/liberty/wlp/usr/servers/orderServer
cp src/main/resources/META-INF/publicKey.pem target/liberty/wlp/usr/servers/orderServer/

# Start the Liberty server
echo "Starting Liberty server..."
mvn liberty:start

# Wait for the server to start
echo "Waiting for server to start..."
sleep 10

# Print URLs for testing
echo ""
echo "Server started. You can access the following URLs:"
echo "- API Documentation: http://localhost:8050/order/openapi/ui"
echo "- Custom Swagger UI: http://localhost:8050/order/swagger.html"
echo "- Home Page: http://localhost:8050/order/index.html"
echo ""
echo "If Swagger UI has CORS issues, use the custom Swagger UI at /swagger.html"
