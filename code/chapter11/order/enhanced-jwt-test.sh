#!/bin/bash
# Enhanced script to test JWT authentication with the Order Service

echo "==== JWT Authentication Test Script ===="
echo "Testing Order Service API with JWT authentication"
echo

# Check if we're inside the tools directory
if [ ! -d "/workspaces/liberty-rest-app/tools" ]; then
  echo "Error: Tools directory not found at /workspaces/liberty-rest-app/tools"
  echo "Make sure you're running this script from the correct location"
  exit 1
fi

# Check if jwtenizr.jar exists
if [ ! -f "/workspaces/liberty-rest-app/tools/jwtenizr.jar" ]; then
  echo "Error: jwtenizr.jar not found in tools directory"
  echo "Please ensure the JWT token generator is available"
  exit 1
fi

# Step 1: Generate a fresh JWT token
echo "Step 1: Generating a fresh JWT token..."
cd /workspaces/liberty-rest-app/tools
java -Dverbose -jar jwtenizr.jar

# Check if token was generated
if [ ! -f "/workspaces/liberty-rest-app/tools/token.jwt" ]; then
  echo "Error: Failed to generate JWT token"
  exit 1
fi

# Read the token
TOKEN=$(cat "/workspaces/liberty-rest-app/tools/token.jwt")
echo "JWT token generated successfully"

# Step 2: Display token information
echo
echo "Step 2: JWT Token Information"
echo "------------------------"

# Get the payload part (second part of the JWT) and decode it
PAYLOAD=$(echo $TOKEN | cut -d. -f2)
DECODED=$(echo $PAYLOAD | base64 -d 2>/dev/null || echo $PAYLOAD | base64 --decode 2>/dev/null)

# Check if jq is installed
if command -v jq &> /dev/null; then
  echo "Token claims:"
  echo $DECODED | jq .
else
  echo "Token claims (install jq for pretty printing):"
  echo $DECODED
fi

# Step 3: Test the protected endpoints
echo
echo "Step 3: Testing protected endpoints"
echo "------------------------"

# Create a test order
echo
echo "Testing POST /api/orders (creating a test order)"
echo "Command: curl -s -X POST -H \"Content-Type: application/json\" -H \"Authorization: Bearer \$TOKEN\" -d http://localhost:8050/order/api/orders"
echo
echo "Response:"
CREATE_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "customerId": "test-user",
    "customerEmail": "test@example.com",
    "status": "PENDING",
    "totalAmount": 99.99,
    "currency": "USD",
    "items": [
      {
        "productId": "prod-123",
        "productName": "Test Product",
        "quantity": 1,
        "unitPrice": 99.99,
        "totalPrice": 99.99
      }
    ],
    "shippingAddress": {
      "street": "123 Test St",
      "city": "Test City",
      "state": "TS",
      "postalCode": "12345",
      "country": "Test Country"
    },
    "billingAddress": {
      "street": "123 Test St",
      "city": "Test City",
      "state": "TS",
      "postalCode": "12345",
      "country": "Test Country"
    }
  }' \
  http://localhost:8050/order/api/orders)
echo "$CREATE_RESPONSE"

# Extract the order ID if present in the response
ORDER_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)

# Test the user endpoint (GET)
echo "Testing GET /api/orders/$ORDER_ID (requires 'user' role)"
echo "Command: curl -s -H \"Authorization: Bearer \$TOKEN\" http://localhost:8050/order/api/orders/$ORDER_ID"
echo
echo "Response:"
RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8050/order/api/orders/$ORDER_ID)
echo "$RESPONSE"

# If the response contains "error", it's likely an error
if [[ "$RESPONSE" == *"error"* ]]; then
  echo
  echo "The request may have failed. Trying with verbose output..."
  curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8050/order/api/orders/1
fi

# Step 4: Admin access test
echo
echo "Step 4: Admin Access Test"
echo "------------------------"
echo "Currently using token with groups: $(echo $DECODED | grep -o '"groups":\[[^]]*\]')"
echo
echo "To test admin endpoint (DELETE /api/orders/{id}):"
echo "1. Edit /workspaces/liberty-rest-app/tools/jwt-token.json"
echo "2. Add 'admin' to the groups array: \"groups\": [\"user\", \"admin\"]"
echo "3. Regenerate the token: cd /workspaces/liberty-rest-app/tools && java -jar jwtenizr.jar"
echo "4. Run this test command:"
if [ -n "$ORDER_ID" ]; then
  echo "   curl -v -X DELETE -H \"Authorization: Bearer \$(cat /workspaces/liberty-rest-app/tools/token.jwt)\" http://localhost:8050/order/api/orders/$ORDER_ID"
else
  echo "   curl -v -X DELETE -H \"Authorization: Bearer \$(cat /workspaces/liberty-rest-app/tools/token.jwt)\" http://localhost:8050/order/api/orders/1"
fi

# Step 5: Troubleshooting tips
echo
echo "Step 5: Troubleshooting Tips"
echo "------------------------"
echo "1. Check JWT configuration in server.xml"
echo "2. Verify public key format in /workspaces/liberty-rest-app/order/src/main/resources/META-INF/publicKey.pem"
echo "3. Run the copy-jwt-key.sh script to ensure the key is in the right location"
echo "4. Verify Liberty server logs: cat /workspaces/liberty-rest-app/order/target/liberty/wlp/usr/servers/orderServer/logs/messages.log | grep -i jwt"
echo
echo "For more details, see: /workspaces/liberty-rest-app/order/JWT-TROUBLESHOOTING.md"
