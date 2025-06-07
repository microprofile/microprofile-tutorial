#!/bin/bash

# Comprehensive Test Script for Payment Service Retry Functionality
# This script tests the MicroProfile Fault Tolerance @Retry annotation and related features
# It combines the functionality of test-retry.sh and test-retry-mechanism.sh

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Check if bc is installed and install it if not
if ! command -v bc &> /dev/null; then
    echo -e "${YELLOW}The 'bc' command is not found. Installing bc...${NC}"
    sudo apt-get update && sudo apt-get install -y bc
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to install bc. Please install it manually.${NC}"
        exit 1
    fi
    echo -e "${GREEN}bc installed successfully.${NC}"
fi

# Header
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     Payment Service Retry Test Suite     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    # GitHub Codespaces environment - use localhost for internal testing
    BASE_URL="http://localhost:9080/payment/api"
    echo -e "${CYAN}Detected GitHub Codespaces environment (using localhost)${NC}"
    echo -e "${CYAN}Note: External access would be https://$CODESPACE_NAME-9080.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN/payment/api${NC}"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    # Gitpod environment
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    echo -e "${CYAN}Detected Gitpod environment${NC}"
else
    # Local or other environment
    BASE_URL="http://localhost:9080/payment/api"
    echo -e "${CYAN}Using local environment${NC}"
fi

echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

# Set up log monitoring
LOG_FILE="/workspaces/liberty-rest-app/payment/target/liberty/wlp/usr/servers/mpServer/logs/messages.log"

# Get initial log position for later analysis
if [ -f "$LOG_FILE" ]; then
    LOG_POSITION=$(wc -l < "$LOG_FILE")
    echo -e "${CYAN}Found server log file at: $LOG_FILE${NC}"
    echo -e "${CYAN}Current log position: $LOG_POSITION${NC}"
    
    # Count initial retry-related messages for comparison
    INITIAL_PROCESSING_COUNT=$(grep -c "Processing payment for amount" "$LOG_FILE" 2>/dev/null || echo 0)
    INITIAL_EXCEPTION_COUNT=$(grep -c "Temporary payment processing failure" "$LOG_FILE" 2>/dev/null || echo 0)
    INITIAL_FALLBACK_COUNT=$(grep -c "Fallback invoked for payment" "$LOG_FILE" 2>/dev/null || echo 0)
    
    # Fix the values to ensure they are clean integers (no newlines, spaces, etc.)
    # and convert multiple zeros to a single zero if needed
    INITIAL_PROCESSING_COUNT=$(echo "$INITIAL_PROCESSING_COUNT" | tr -d '\n' | tr -d ' ')
    INITIAL_PROCESSING_COUNT=${INITIAL_PROCESSING_COUNT:-0}
    # Remove leading zeros and handle case where it's all zeros
    INITIAL_PROCESSING_COUNT=$(echo "$INITIAL_PROCESSING_COUNT" | sed 's/^0*//')
    INITIAL_PROCESSING_COUNT=${INITIAL_PROCESSING_COUNT:-0}
    
    INITIAL_EXCEPTION_COUNT=$(echo "$INITIAL_EXCEPTION_COUNT" | tr -d '\n' | tr -d ' ')
    INITIAL_EXCEPTION_COUNT=${INITIAL_EXCEPTION_COUNT:-0}
    # Remove leading zeros and handle case where it's all zeros
    INITIAL_EXCEPTION_COUNT=$(echo "$INITIAL_EXCEPTION_COUNT" | sed 's/^0*//')
    INITIAL_EXCEPTION_COUNT=${INITIAL_EXCEPTION_COUNT:-0}
    
    INITIAL_FALLBACK_COUNT=$(echo "$INITIAL_FALLBACK_COUNT" | tr -d '\n' | tr -d ' ')
    INITIAL_FALLBACK_COUNT=${INITIAL_FALLBACK_COUNT:-0}
    # Remove leading zeros and handle case where it's all zeros
    INITIAL_FALLBACK_COUNT=$(echo "$INITIAL_FALLBACK_COUNT" | sed 's/^0*//')
    INITIAL_FALLBACK_COUNT=${INITIAL_FALLBACK_COUNT:-0}
    
    echo -e "${CYAN}Initial processing count: $INITIAL_PROCESSING_COUNT${NC}"
    echo -e "${CYAN}Initial exception count: $INITIAL_EXCEPTION_COUNT${NC}"
    echo -e "${CYAN}Initial fallback count: $INITIAL_FALLBACK_COUNT${NC}"
else
    LOG_POSITION=0
    INITIAL_PROCESSING_COUNT=0
    INITIAL_EXCEPTION_COUNT=0 
    INITIAL_FALLBACK_COUNT=0
    echo -e "${YELLOW}Warning: Server log file not found at: $LOG_FILE${NC}"
    echo -e "${YELLOW}Will continue without log analysis${NC}"
fi

echo ""

# Function to make HTTP requests and display results
make_request() {
    local method=$1
    local url=$2
    local description=$3
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo -e "${CYAN}Request: $method $url${NC}"
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
    
    echo -e "${CYAN}Response: $body${NC}"
    echo -e "${CYAN}Total time: ${total_time}ms (curl: ${curl_time}s)${NC}"
    echo ""
    echo -e "${BLUE}----------------------------------------${NC}"
    echo ""
}

# Show PaymentService fault tolerance configuration
echo -e "${BLUE}=== PaymentService Fault Tolerance Configuration ===${NC}"
echo -e "${YELLOW}Your PaymentService has these retry settings:${NC}"
echo -e "${CYAN}• Max Retries: 3${NC}"
echo -e "${CYAN}• Delay: 2000ms${NC}"
echo -e "${CYAN}• Jitter: 500ms${NC}"
echo -e "${CYAN}• Retry on: PaymentProcessingException${NC}"
echo -e "${CYAN}• Abort on: CriticalPaymentException${NC}"
echo -e "${CYAN}• Processing delay: 1500ms per attempt${NC}"
echo ""
echo -e "${YELLOW}🔍 HOW TO IDENTIFY RETRY BEHAVIOR:${NC}"
echo -e "${CYAN}• ⚡ Fast response (~1.5s) = Succeeded on 1st attempt${NC}"
echo -e "${CYAN}• 🔄 Medium response (~4s) = Needed 1 retry${NC}"
echo -e "${CYAN}• 🔄🔄 Slow response (~6.5s) = Needed 2 retries${NC}" 
echo -e "${CYAN}• 🔄🔄🔄 Very slow response (~9-12s) = Needed 3 retries${NC}"
echo ""

echo -e "${YELLOW}Make sure the Payment Service is running on port 9080${NC}"
echo -e "${YELLOW}You can start it with: cd payment && mvn liberty:run${NC}"
echo ""
read -p "Press Enter to continue..." 
echo ""

# ====================================
# PART 1: Standard Test Cases
# ====================================
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     PART 1: Standard Retry Test Cases     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

# Test 1: Valid payment (should succeed, may need retries due to random failures)
echo -e "${BLUE}=== Test 1: Valid Payment Authorization ===${NC}"
echo -e "${YELLOW}This test uses a valid amount and may succeed immediately or after retries${NC}"
echo -e "${YELLOW}Expected: Success after 1-4 attempts (due to 30% failure simulation)${NC}"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=100.50" \
    "Valid payment amount (100.50) - may trigger retries due to random failures"

# Test 2: Another valid payment to see retry behavior
echo -e "${BLUE}=== Test 2: Another Valid Payment ===${NC}"
echo -e "${YELLOW}Running another test to demonstrate retry variability${NC}"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=250.00" \
    "Valid payment amount (250.00) - testing retry behavior"

# Test 3: Invalid payment amount (should abort immediately)
echo -e "${BLUE}=== Test 3: Invalid Payment Amount (Abort Condition) ===${NC}"
echo -e "${YELLOW}This test uses an invalid amount which should trigger CriticalPaymentException${NC}"
echo -e "${YELLOW}Expected: Immediate failure with no retries${NC}"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=0" \
    "Invalid payment amount (0) - should abort immediately with CriticalPaymentException"

# Test 4: Negative amount (should abort immediately)
echo -e "${BLUE}=== Test 4: Negative Payment Amount ===${NC}"
echo -e "${YELLOW}Expected: Immediate failure with no retries${NC}"
echo ""
make_request "POST" "$BASE_URL/authorize?amount=-50" \
    "Negative payment amount (-50) - should abort immediately"

# Test 5: No amount parameter (should abort immediately)
echo -e "${BLUE}=== Test 5: Missing Payment Amount ===${NC}"
echo -e "${YELLOW}Expected: Immediate failure with no retries${NC}"
echo ""
make_request "POST" "$BASE_URL/authorize" \
    "Missing payment amount - should abort immediately"

# ====================================
# PART 2: Focused Retry Analysis
# ====================================
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     PART 2: Focused Retry Analysis     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

# Send multiple requests to observe retry behavior
echo -e "${BLUE}=== Multiple Requests to Observe Retry Patterns ===${NC}"
echo -e "${YELLOW}Sending requests that will likely trigger retries...${NC}"
echo -e "${YELLOW}(Our code has a 30% chance of failure, which should trigger retries)${NC}"
echo ""

# Send multiple requests to increase chance of seeing retry behavior
for i in {1..5}; do
    echo -e "${PURPLE}[Request $i/5] Sending request...${NC}"
    amount=$((100 + i * 25))
    make_request "POST" "$BASE_URL/authorize?amount=$amount" \
        "Payment request $i with amount $amount"
    
    # Small delay between requests
    sleep 2
done

# Wait for all retries to complete
echo -e "${YELLOW}Waiting 10 seconds for all retries to complete...${NC}"
sleep 10

# ====================================
# PART 3: Log Analysis
# ====================================
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     PART 3: Log Analysis     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

if [ -f "$LOG_FILE" ]; then
    # Count final retry-related messages
    FINAL_PROCESSING_COUNT=$(grep -c "Processing payment for amount" "$LOG_FILE" 2>/dev/null || echo 0)
    FINAL_EXCEPTION_COUNT=$(grep -c "Temporary payment processing failure" "$LOG_FILE" 2>/dev/null || echo 0)
    FINAL_FALLBACK_COUNT=$(grep -c "Fallback invoked for payment" "$LOG_FILE" 2>/dev/null || echo 0)
    
    # Ensure values are proper integers (removing any newlines, spaces, or leading zeros)
    FINAL_PROCESSING_COUNT=$(echo "$FINAL_PROCESSING_COUNT" | tr -d '\n' | tr -d ' ' | sed 's/^0*//')
    FINAL_EXCEPTION_COUNT=$(echo "$FINAL_EXCEPTION_COUNT" | tr -d '\n' | tr -d ' ' | sed 's/^0*//')
    FINAL_FALLBACK_COUNT=$(echo "$FINAL_FALLBACK_COUNT" | tr -d '\n' | tr -d ' ' | sed 's/^0*//')
    
    # If values are empty after cleaning, set them to 0
    FINAL_PROCESSING_COUNT=${FINAL_PROCESSING_COUNT:-0}
    FINAL_EXCEPTION_COUNT=${FINAL_EXCEPTION_COUNT:-0}
    FINAL_FALLBACK_COUNT=${FINAL_FALLBACK_COUNT:-0}
    
    # Also ensure initial values are proper integers
    INITIAL_PROCESSING_COUNT=$(echo "${INITIAL_PROCESSING_COUNT:-0}" | tr -d '\n' | tr -d ' ' | sed 's/^0*//')
    INITIAL_EXCEPTION_COUNT=$(echo "${INITIAL_EXCEPTION_COUNT:-0}" | tr -d '\n' | tr -d ' ' | sed 's/^0*//')
    INITIAL_FALLBACK_COUNT=$(echo "${INITIAL_FALLBACK_COUNT:-0}" | tr -d '\n' | tr -d ' ' | sed 's/^0*//')
    
    # If values are empty after cleaning, set them to 0
    INITIAL_PROCESSING_COUNT=${INITIAL_PROCESSING_COUNT:-0}
    INITIAL_EXCEPTION_COUNT=${INITIAL_EXCEPTION_COUNT:-0}
    INITIAL_FALLBACK_COUNT=${INITIAL_FALLBACK_COUNT:-0}
    
    NEW_PROCESSING=$((FINAL_PROCESSING_COUNT - INITIAL_PROCESSING_COUNT))
    NEW_EXCEPTIONS=$((FINAL_EXCEPTION_COUNT - INITIAL_EXCEPTION_COUNT))
    NEW_FALLBACKS=$((FINAL_FALLBACK_COUNT - INITIAL_FALLBACK_COUNT))
    
    echo -e "${CYAN}New payment processing attempts: $NEW_PROCESSING${NC}"
    echo -e "${CYAN}New exceptions triggered: $NEW_EXCEPTIONS${NC}"
    echo -e "${CYAN}New fallback invocations: $NEW_FALLBACKS${NC}"
    
    # Calculate retry statistics
    EXPECTED_ATTEMPTS=10  # We sent 10 valid requests in total
    
    if [ "${NEW_PROCESSING:-0}" -gt 0 ]; then
        AVG_ATTEMPTS_PER_REQUEST=$(echo "scale=2; ${NEW_PROCESSING:-0} / ${EXPECTED_ATTEMPTS:-1}" | bc)
        echo -e "${CYAN}Average processing attempts per request: $AVG_ATTEMPTS_PER_REQUEST${NC}"
        
        if [ "${NEW_EXCEPTIONS:-0}" -gt 0 ]; then
            RETRY_RATE=$(echo "scale=2; ${NEW_EXCEPTIONS:-0} / ${NEW_PROCESSING:-1} * 100" | bc)
            echo -e "${CYAN}Retry rate: $RETRY_RATE% of attempts failed and triggered retry${NC}"
        else
            echo -e "${CYAN}Retry rate: 0% (no exceptions triggered)${NC}"
        fi
        
        if [ "${NEW_FALLBACKS:-0}" -gt 0 ]; then
            FALLBACK_RATE=$(echo "scale=2; ${NEW_FALLBACKS:-0} / ${EXPECTED_ATTEMPTS:-1} * 100" | bc)
            echo -e "${CYAN}Fallback rate: $FALLBACK_RATE% of requests ended with fallback${NC}"
        else
            echo -e "${CYAN}Fallback rate: 0% (no fallbacks triggered)${NC}"
        fi
    fi
    
    # Extract the latest log entries related to retries
    echo ""
    echo -e "${BLUE}Latest server log entries related to retries and fallbacks:${NC}"
    RETRY_LOGS=$(tail -n +$LOG_POSITION "$LOG_FILE" | grep -E "Processing payment for amount|Temporary payment processing failure|Fallback invoked for payment|Retry|Timeout" | tail -20)
    
    if [ -n "$RETRY_LOGS" ]; then
        echo "$RETRY_LOGS"
    else
        echo -e "${RED}No relevant log entries found.${NC}"
    fi
else
    echo -e "${RED}Log file not found for analysis${NC}"
fi

# ====================================
# Summary and Conclusion
# ====================================
echo ""
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     Test Summary and Conclusion     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

echo -e "${GREEN}=== Retry Testing Complete ===${NC}"
echo ""
echo -e "${YELLOW}Key observations:${NC}"
echo -e "${CYAN}1. Look for multiple 'Processing payment' entries with the same amount - shows retry attempts${NC}"
echo -e "${CYAN}2. 'PaymentProcessingException' indicates a failure that triggered retry${NC}"
echo -e "${CYAN}3. After max retries (3), the fallback method is called${NC}"
echo -e "${CYAN}4. Time delays between retries (2000ms + jitter up to 500ms) demonstrate backoff strategy${NC}"
echo -e "${CYAN}5. Successful requests complete in ~1.5-12 seconds depending on retries${NC}"
echo -e "${CYAN}6. Abort conditions (invalid amounts) fail immediately (~1.5 seconds)${NC}"
echo ""
echo -e "${YELLOW}For more detailed retry logs, you can monitor the server logs directly:${NC}"
echo -e "${CYAN}tail -f $LOG_FILE${NC}"
