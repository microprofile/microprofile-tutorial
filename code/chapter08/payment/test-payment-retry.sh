#!/bin/bash

# Test script for Payment Service Retry functionality
# Tests the @Retry annotation on the authorizePayment method

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    BASE_URL="http://localhost:9080/payment/api"
    echo -e "${CYAN}Detected GitHub Codespaces environment${NC}"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    echo -e "${CYAN}Detected Gitpod environment${NC}"
else
    BASE_URL="http://localhost:9080/payment/api"
    echo -e "${CYAN}Using local environment${NC}"
fi

echo ""
echo -e "${BLUE}=== Payment Service Retry Test ===${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

echo -e "${YELLOW}Retry Configuration:${NC}"
echo "  • Max Retries: 3"
echo "  • Delay: 2000ms"
echo "  • Jitter: 500ms"
echo "  • Retry on: PaymentProcessingException"
echo "  • Abort on: CriticalPaymentException"
echo "  • Simulated failure rate: 60%"
echo "  • Processing delay: 1500ms per attempt"
echo ""

echo -e  "${CYAN}🔍 How to identify retry behavior:${NC}"
echo "  • ⚡ Fast   (~1.5s)    = Success on 1st attempt (1500ms processing)"
echo "  • 🔄 Medium (~5s)      = Succeeded after 1 retry (1500ms + 1500-2500ms delay + 1500ms)"
echo "  • 🔄🔄 Slow  (~8.5s)   = Succeeded after 2 retries"
echo "  • 🔄🔄🔄 Very slow (~12s) = Needed all 3 retries or fallback activated"
echo ""

# Function to make HTTP requests and display results
make_request() {
    local amount=$1
    local description=$2
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo -e "${CYAN}Amount: \$$amount${NC}"
    
    start_time=$(date +%s.%N)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/authorize?amount=${amount}" 2>/dev/null || echo "HTTP_STATUS:000")
    end_time=$(date +%s.%N)
    
    duration=$(echo "$end_time - $start_time" | bc)
    duration_formatted=$(printf "%.2f" $duration)
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    # Determine retry behavior based on duration
    duration_int=$(echo "$duration * 10" | bc | cut -d. -f1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if [ "$duration_int" -lt 20 ]; then
            echo -e "${GREEN}✓ Success (HTTP $http_code) - First attempt! ⚡${NC}"
        elif [ "$duration_int" -lt 65 ]; then
            echo -e "${GREEN}✓ Success (HTTP $http_code) - After 1 retry 🔄${NC}"
        elif [ "$duration_int" -lt 105 ]; then
            echo -e "${GREEN}✓ Success (HTTP $http_code) - After 2 retries 🔄🔄${NC}"
        else
            echo -e "${GREEN}✓ Success (HTTP $http_code) - After 3 retries or fallback 🔄🔄🔄${NC}"
        fi
        echo -e "${GREEN}Response: $body${NC}"
    elif [ "$http_code" -ge 400 ]; then
        echo -e "${YELLOW}⚠ Error (HTTP $http_code) - Aborted immediately, no retries fired${NC}"
        echo -e "${YELLOW}  (CriticalPaymentException aborts the retry chain)${NC}"
        echo -e "${YELLOW}Response: $body${NC}"
    else
        echo -e "${RED}✗ Unexpected response (HTTP $http_code)${NC}"
        echo -e "${RED}Response: $body${NC}"
    fi
    
    echo -e "${CYAN}Duration: ${duration_formatted}s${NC}"
    echo ""
    echo "----------------------------------------"
    echo ""
}

# Run multiple tests
echo -e "${BLUE}=== Test 1: Valid Payment (Will Likely Trigger Retries) ===${NC}"
make_request "100.50" "Valid payment - 60% failure rate per attempt"

echo -e "${BLUE}=== Test 2: Another Valid Payment ===${NC}"
make_request "250.00" "Valid payment - observe retry behavior"

echo -e "${BLUE}=== Test 3: Large Payment ===${NC}"
make_request "999.99" "Large payment - testing retry behavior"

echo -e "${BLUE}=== Test 4: Invalid Amount (Abort Condition) ===${NC}"
make_request "0" "Invalid amount - should abort immediately (no retries)"

echo -e "${BLUE}=== Test 5: Negative Amount (Abort Condition) ===${NC}"
make_request "-50" "Negative amount - should abort immediately (no retries)"

echo -e "${BLUE}=== Test 6: Multiple Requests to Observe Patterns ===${NC}"
echo -e "${CYAN}Sending 5 requests to observe retry patterns...${NC}"
echo ""

for i in {1..5}; do
    amount=$((50 + RANDOM % 150))
    echo -e "${YELLOW}Request $i:${NC}"
    make_request "$amount" "Random amount test #$i"
    sleep 1
done

echo -e "${GREEN}=== Retry Test Complete ===${NC}"
echo ""

# Show MicroProfile Fault Tolerance retry metrics
echo -e "${BLUE}=== MicroProfile Fault Tolerance Retry Metrics ===${NC}"
echo -e "${CYAN}Command: curl http://localhost:9080/metrics?scope=base | grep retry${NC}"
echo ""

metrics=$(curl -s "http://localhost:9080/metrics?scope=base" | grep retry)

if [ -n "$metrics" ]; then
    echo -e "${GREEN}Retry metrics collected during this test run:${NC}"
    echo ""
    echo "$metrics" | while IFS= read -r line; do
        echo -e "  ${YELLOW}$line${NC}"
    done
    echo ""
    echo -e "${CYAN}Metric key:${NC}"
    echo "  • ft_retry_calls_total{retried=\"true\"}   — calls that triggered at least one retry"
    echo "  • ft_retry_calls_total{retried=\"false\"}  — calls that succeeded on the first attempt"
    echo "  • ft_retry_retries_total                  — total individual retry attempts fired"
else
    echo -e "${YELLOW}No retry metrics returned. The server may not have started yet, or no${NC}"
    echo -e "${YELLOW}requests completed. Try re-running the script after the server is ready.${NC}"
fi

echo ""
