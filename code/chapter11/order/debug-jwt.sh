#!/bin/bash
# Script to debug JWT authentication issues

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

# Check for missing token
if [ -z "$TOKEN" ]; then
  echo "Error: Empty JWT token"
  exit 1
fi

# Print token details
echo "=== JWT Token Details ==="
echo "First 50 characters of token: ${TOKEN:0:50}..."
echo "Token length: ${#TOKEN}"
echo ""

# Display token headers
echo "=== JWT Token Headers ==="
HEADER=$(echo $TOKEN | cut -d. -f1)
DECODED_HEADER=$(echo $HEADER | base64 -d 2>/dev/null || echo $HEADER | base64 --decode 2>/dev/null)
echo "$DECODED_HEADER" | jq . 2>/dev/null || echo "$DECODED_HEADER"
echo ""

# Display token payload
echo "=== JWT Token Payload ==="
PAYLOAD=$(echo $TOKEN | cut -d. -f2)
DECODED=$(echo $PAYLOAD | base64 -d 2>/dev/null || echo $PAYLOAD | base64 --decode 2>/dev/null)
echo "$DECODED" | jq . 2>/dev/null || echo "$DECODED"
echo ""

# Test the endpoint with verbose output
echo "=== Testing Endpoint with JWT Token ==="
echo "GET http://localhost:8050/order/api/orders/1"
echo "Authorization: Bearer ${TOKEN:0:50}..."
echo ""
echo "CURL Response Headers:"
curl -v -s -o /dev/null -H "Authorization: Bearer $TOKEN" "http://localhost:8050/order/api/orders/1" 2>&1 | grep -i '> ' 
echo ""
echo "CURL Response:"
curl -v -H "Authorization: Bearer $TOKEN" "http://localhost:8050/order/api/orders/1"
echo ""

# Check server logs for JWT-related messages
echo "=== Recent JWT-related Log Messages ==="
find /workspaces/liberty-rest-app/order/target/liberty/wlp/usr/servers/orderServer/logs -name "*.log" -exec grep -l "jwt\|JWT\|auth" {} \; | xargs tail -n 30 2>/dev/null
echo ""

echo "=== Debug Complete ==="
echo "If you still have issues, check detailed logs in the Liberty server logs directory:"
echo "/workspaces/liberty-rest-app/order/target/liberty/wlp/usr/servers/orderServer/logs/"
