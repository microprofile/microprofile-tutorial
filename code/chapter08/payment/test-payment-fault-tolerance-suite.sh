#!/bin/bash

# Test script for Payment Service Fault Tolerance features
# This script demonstrates retry policies, circuit breakers, and fallback mechanisms

set -e

echo "=== Payment Service Fault Tolerance Test ==="
echo ""

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    # GitHub Codespaces environment
    BASE_URL="https://$CODESPACE_NAME-9080.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN/payment/api"
    echo "Detected GitHub Codespaces environment"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    # Gitpod environment
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    echo "Detected Gitpod environment"
else
    # Local or other environment - try to detect hostname
    HOSTNAME=$(hostname)
    BASE_URL="http://$HOSTNAME:9080/payment/api"
    echo "Using hostname: $HOSTNAME"
fi

echo "Base URL: $BASE_URL"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to make HTTP requests and display results
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo "Request: $method $url"
    if [ -n "$data" ]; then
        echo "Data: $data"
    fi
    echo ""
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$url" \
            -H "Content-Type: application/json" \
            -d "$data" 2>/dev/null || echo "HTTP_STATUS:000")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$url" 2>/dev/null || echo "HTTP_STATUS:000")
    fi
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ Success (HTTP $http_code)${NC}"
    elif [ "$http_code" -ge 400 ] && [ "$http_code" -lt 500 ]; then
        echo -e "${YELLOW}⚠ Client Error (HTTP $http_code)${NC}"
    else
        echo -e "${RED}✗ Server Error (HTTP $http_code)${NC}"
    fi
    
    echo "Response: $body"
    echo ""
    echo "----------------------------------------"
    echo ""
}

echo "Make sure the Payment Service is running on port 9080"
echo "You can start it with: cd payment && mvn liberty:run"
echo ""
read -p "Press Enter to continue..."
echo ""

# Test 1: Successful payment authorization
echo -e "${BLUE}=== Test 1: Successful Payment Authorization ===${NC}"
make_request "POST" "$BASE_URL/authorize" \
    '{"cardNumber":"4111111111111111","cardHolderName":"Test User","expiryDate":"12/25","securityCode":"123","amount":100.00}' \
    "Normal payment authorization (should succeed)"

# Test 2: Payment authorization with retry (failure scenario)
echo -e "${BLUE}=== Test 2: Payment Authorization with Retry ===${NC}"
make_request "POST" "$BASE_URL/authorize" \
    '{"cardNumber":"4111111111110000","cardHolderName":"Test User","expiryDate":"12/25","securityCode":"123","amount":100.00}' \
    "Payment authorization with card ending in 0000 (triggers retries and fallback)"

# Test 3: Payment verification (random failures)
echo -e "${BLUE}=== Test 3: Payment Verification with Aggressive Retry ===${NC}"
make_request "POST" "$BASE_URL/verify?transactionId=TXN1234567890" "" \
    "Payment verification (may succeed or trigger fallback)"

# Test 4: Payment capture with circuit breaker
echo -e "${BLUE}=== Test 4: Payment Capture with Circuit Breaker ===${NC}"
for i in {1..5}; do
    echo "Attempt $i/5:"
    make_request "POST" "$BASE_URL/capture?transactionId=TXN$i" "" \
        "Payment capture attempt $i (circuit breaker may trip)"
    sleep 1
done

# Test 5: Payment refund with conservative retry
echo -e "${BLUE}=== Test 5: Payment Refund with Conservative Retry ===${NC}"
make_request "POST" "$BASE_URL/refund?transactionId=TXN1234567890&amount=50.00" "" \
    "Payment refund with valid amount"

# Test 6: Payment refund with invalid amount (abort condition)
echo -e "${BLUE}=== Test 6: Payment Refund with Invalid Amount ===${NC}"
make_request "POST" "$BASE_URL/refund?transactionId=TXN1234567890&amount=" "" \
    "Payment refund with empty amount (should abort immediately)"

# Test 7: Configuration check
echo -e "${BLUE}=== Test 7: Configuration Check ===${NC}"
make_request "GET" "$BASE_URL/payment-config" "" \
    "Get current payment configuration including fault tolerance settings"

echo -e "${GREEN}=== Fault Tolerance Testing Complete ===${NC}"
echo ""
echo "Key observations:"
echo "• Authorization retries: Watch for 3 retry attempts with card ending in 0000"
echo "• Verification retries: Up to 5 attempts with random failures"
echo "• Circuit breaker: Multiple capture failures should open the circuit"
echo "• Fallback responses: Failed operations return graceful degradation messages"
echo "• Conservative refund: Only 1 retry attempt, immediate abort on invalid input"
echo ""
echo "Monitor server logs for detailed retry and fallback behavior:"
echo "tail -f target/liberty/wlp/usr/servers/mpServer/logs/messages.log"
