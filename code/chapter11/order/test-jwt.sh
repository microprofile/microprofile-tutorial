#!/bin/bash
# Script to test JWT authentication with the Order Service

# Check tools directory
if [ ! -d "/workspaces/liberty-rest-app/tools" ]; then
  echo "Error: Tools directory not found!"
  exit 1
fi

# Get the JWT token
TOKEN_FILE="/workspaces/liberty-rest-app/tools/token.jwt"
if [ ! -f "$TOKEN_FILE" ]; then
  echo "JWT token not found at $TOKEN_FILE"
  echo "Generating a new token..."
  cd /workspaces/liberty-rest-app/tools
  java -Dverbose -jar jwtenizr.jar
fi

# Read the token
TOKEN=$(cat "$TOKEN_FILE")

# Test User Endpoint (GET order)
echo "Testing GET order with user role..."
echo "URL: http://localhost:8050/order/api/orders/1"
curl -v -H "Authorization: Bearer $TOKEN" "http://localhost:8050/order/api/orders/1"

echo -e "\n\nChecking token payload:"
# Get the payload part (second part of the JWT) and decode it
PAYLOAD=$(echo $TOKEN | cut -d. -f2)
DECODED=$(echo $PAYLOAD | base64 -d 2>/dev/null || echo $PAYLOAD | base64 --decode 2>/dev/null)
echo $DECODED | jq . 2>/dev/null || echo $DECODED

echo -e "\n\nIf you need admin access, edit the JWT roles in /workspaces/liberty-rest-app/tools/jwt-token.json"
echo "Add 'admin' to the groups array, then regenerate the token with: java -jar tools/jwtenizr.jar"
