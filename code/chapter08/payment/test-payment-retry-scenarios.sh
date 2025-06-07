#!/bin/bash

# Test script for Payment Service Retry functionality
# Tests the @Retry annotation on the processPayment method

set -e

echo "=== Payment Service Retry Test ==="
echo ""

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    # GitHub Codespaces environment - use localhost for internal testing
    BASE_URL="http://localhost:9080/payment/api"
    echo "Detected GitHub Codespaces environment (using localhost)"
    echo "Note: External access would be https://$CODESPACE_NAME-9080.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN/payment/api"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    # Gitpod environment
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    echo "Detected Gitpod environment"
else
    # Local or other environment
    BASE_URL="http://localhost:9080/payment/api"
    echo "Using local environment"
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
    local description=$3
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo "Request: $method $url"
    echo ""
    
    # Capture start time
    start_time=$(date +%s%3N)
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}\nTIME_TOTAL:%{time_total}" -X $method "$url" 2>/dev/null || echo "HTTP_STATUS:000")
    
    # Capture end time
    end_time=$(date +%s%3N)
    total_time=$((end_time - start_time))
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    curl_time=$(echo "$response" | grep "TIME_TOTAL:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d' | sed '/TIME_TOTAL:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        # Analyze timing to determine retry behavior
        # Convert curl_time to integer for comparison (multiply by 10 to handle decimals)
        curl_time_int=$(echo "$curl_time" | awk '{printf "%.0f", $1 * 10}')
        
        if [ "$curl_time_int" -lt 20 ]; then  # < 2.0 seconds
            echo -e "${GREEN}✓ Success (HTTP $http_code) - First attempt! ⚡${NC}"
        elif [ "$curl_time_int" -lt 55 ]; then  # < 5.5 seconds
            echo -e "${GREEN}✓ Success (HTTP $http_code) - After 1 retry 🔄${NC}"
        elif [ "$curl_time_int" -lt 80 ]; then  # < 8.0 seconds
            echo -e "${GREEN}✓ Success (HTTP $http_code) - After 2 retries 🔄🔄${NC}"
        else
            echo -e "${GREEN}✓ Success (HTTP $http_code) - After 3 retries 🔄🔄🔄${NC}"
        fi
    elif [ "$http_code" -ge 400 ] && [ "$http_code" -lt 500 ]; then
        echo -e "${YELLOW}⚠ Client Error (HTTP $http_code)${NC}"
    else
        echo -e "${RED}✗ Server Error (HTTP $http_code)${NC}"
    fi
    
    echo "Response: $body"
    echo "Total time: ${total_time}ms (curl: ${curl_time}s)"
    echo ""
    echo "----------------------------------------"
    echo ""
}

echo "Starting Payment Service Retry Tests..."
echo ""
echo "Your PaymentService has these retry settings:"
echo "• Max Retries: 3"
echo "• Delay: 2000ms"
echo "• Jitter: 500ms"
echo "• Retry on: PaymentProcessingException"
echo "• Abort on: CriticalPaymentException"
echo "• Simulated failure rate: 30% (Math.random() > 0.7)"
echo "• Processing delay: 1500ms per attempt"
echo ""
echo "🔍 HOW TO IDENTIFY RETRY BEHAVIOR:"
echo "• ⚡ Fast response (~1.5s) = Succeeded on 1st attempt"
echo "• 🔄 Medium response (~4s) = Needed 1 retry"
echo "• 🔄🔄 Slow response (~6.5s) = Needed 2 retries" 
echo "• 🔄🔄🔄 Very slow response (~9-12s) = Needed 3 retries"
echo ""

echo "Make sure the Payment Service is running on port 9080"
echo "You can start it with: cd payment && mvn liberty:run"
echo ""
read -p "Press Enter to continue..."
echo ""

# Test 1: Valid payment (should succeed, may need retries due to random failures)
echo -e "${BLUE}=== Test 1: Valid Payment Authorization ===${NC}"
echo "This test uses a valid amount and may succeed immediately or after retries"
echo "Expected: Success after 1-4 attempts (due to 30% failure simulation)"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=100.50" \
    "Valid payment amount (100.50) - may trigger retries due to random failures"

# Test 2: Another valid payment to see retry behavior
echo -e "${BLUE}=== Test 2: Another Valid Payment ===${NC}"
echo "Running another test to demonstrate retry variability"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=250.00" \
    "Valid payment amount (250.00) - testing retry behavior"

# Test 3: Invalid payment amount (should abort immediately)
echo -e "${BLUE}=== Test 3: Invalid Payment Amount (Abort Condition) ===${NC}"
echo "This test uses an invalid amount which should trigger CriticalPaymentException"
echo "Expected: Immediate failure with no retries"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=0" \
    "Invalid payment amount (0) - should abort immediately with CriticalPaymentException"

# Test 4: Negative amount (should abort immediately)
echo -e "${BLUE}=== Test 4: Negative Payment Amount ===${NC}"
echo "Expected: Immediate failure with no retries"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=-50" \
    "Negative payment amount (-50) - should abort immediately"

# Test 5: No amount parameter (should abort immediately)
echo -e "${BLUE}=== Test 5: Missing Payment Amount ===${NC}"
echo "Expected: Immediate failure with no retries"
echo ""
make_request "POST" "$BASE_URL/authorize" \
    "Missing payment amount - should abort immediately"

# Test 6: Multiple requests to observe retry patterns
echo -e "${BLUE}=== Test 6: Multiple Requests to Observe Retry Patterns ===${NC}"
echo "Running 5 valid payment requests to observe retry behavior patterns"
echo ""

for i in {1..5}; do
    echo "Request $i/5:"
    amount=$((100 + i * 25))
    make_request "POST" "$BASE_URL/authorize?amount=$amount" \
        "Payment request $i with amount $amount"
    
    # Small delay between requests
    sleep 2
done

echo -e "${GREEN}=== Retry Testing Complete ===${NC}"
echo ""
echo "Key observations to look for:"
echo -e "• ${GREEN}Successful requests:${NC} Should complete in ~1.5-12 seconds depending on retries"
echo -e "• ${YELLOW}Retry behavior:${NC} Failed attempts will retry up to 3 times with 2-2.5 second delays"
echo -e "• ${RED}Abort conditions:${NC} Invalid amounts should fail immediately (~1.5 seconds)"
echo -e "• ${BLUE}Random failures:${NC} ~30% of valid requests may need retries"
echo ""
echo "To see detailed retry logs, monitor the server logs:"
echo "tail -f target/liberty/wlp/usr/servers/mpServer/logs/messages.log"
echo ""
echo "Expected timing patterns:"
echo "• Success on 1st attempt: ~1.5 seconds"
echo "• Success on 2nd attempt: ~4-4.5 seconds"
echo "• Success on 3rd attempt: ~6.5-7.5 seconds"
echo "• Success on 4th attempt: ~9-10.5 seconds"
echo "• Abort conditions: ~1.5 seconds (no retries)"
