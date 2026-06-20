#!/bin/bash

# Test script for Payment Service Bulkhead functionality
# This script demonstrates the @Bulkhead annotation by sending many concurrent requests
# and observing how the service handles concurrent load up to its configured limit (5)

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
echo -e "${BLUE}     Payment Service Bulkhead Test     ${NC}"
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
LOG_FILE="/workspaces/microprofile-tutorial/code/chapter08/payment/target/liberty/wlp/usr/servers/mpServer/logs/messages.log"

# Get initial log position for later analysis
if [ -f "$LOG_FILE" ]; then
    LOG_POSITION=$(wc -l < "$LOG_FILE")
    echo -e "${CYAN}Found server log file at: $LOG_FILE${NC}"
    echo -e "${CYAN}Current log position: $LOG_POSITION${NC}"
else
    LOG_POSITION=0
    echo -e "${YELLOW}Warning: Server log file not found at: $LOG_FILE${NC}"
    echo -e "${YELLOW}Will continue without log analysis${NC}"
fi

echo ""

# ====================================
# Bulkhead Configuration
# ====================================
echo -e "${BLUE}=== PaymentService Bulkhead Configuration ===${NC}"
echo -e "${YELLOW}Your PaymentService has these bulkhead settings:${NC}"
echo -e "${CYAN}• Maximum Concurrent Threads: 5${NC}"
echo -e "${CYAN}• Waiting Task Queue: 2${NC}"
echo -e "${CYAN}• Effective Capacity: 7 (5 running + 2 queued)${NC}"
echo -e "${CYAN}• Asynchronous Processing: Yes${NC}"
echo -e "${CYAN}• Retry on failure: Yes (3 retries)${NC}"
echo -e "${CYAN}• Processing delay: ~1.5 seconds per attempt${NC}"
echo ""

echo -e "${YELLOW}WHAT TO EXPECT WITH BULKHEAD (10 concurrent requests):${NC}"
echo -e "${CYAN}• 5 requests processed immediately, 2 queued — 7 total accepted${NC}"
echo -e "${CYAN}• 3 requests rejected instantly with 'Bulkhead full' status${NC}"
echo -e "${CYAN}• Accepted requests complete in ~1.5 seconds${NC}"
echo ""

echo -e "${YELLOW}Make sure the Payment Service is running on port 9080${NC}"
echo -e "${YELLOW}You can start it with: cd payment && mvn liberty:run${NC}"
echo ""
read -p "Press Enter to continue..." 
echo ""

# ====================================
# Test 1: Single Request (Baseline)
# ====================================
echo -e "${BLUE}=== Test 1: Single Request (Baseline) ===${NC}"
echo -e "${YELLOW}This test sends a single request to establish baseline performance${NC}"
echo ""

# Function to send a request and measure time
send_request() {
    local id=$1
    local amount=$2
    local start_time=$(date +%s.%N)
    
    response=$(curl -s -X POST "${BASE_URL}/authorize?amount=${amount}")
    status=$?
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc)
    duration=$(printf "%.2f" $duration)
    
    if [ $status -eq 0 ]; then
        if echo "$response" | grep -q "success"; then
            echo -e "${GREEN}[Request $id] SUCCESS: Payment processed in ${duration}s${NC}"
            echo -e "${GREEN}[Request $id] Response: $response${NC}"
        elif echo "$response" | grep -q "rejected"; then
            echo -e "${YELLOW}[Request $id] REJECTED: Bulkhead full (took ${duration}s)${NC}"
            echo -e "${YELLOW}[Request $id] Response: $response${NC}"
        elif echo "$response" | grep -q "failed"; then
            echo -e "${PURPLE}[Request $id] FALLBACK: Service used fallback (took ${duration}s)${NC}"
            echo -e "${PURPLE}[Request $id] Response: $response${NC}"
        else
            echo -e "${RED}[Request $id] ERROR: Unexpected response (took ${duration}s)${NC}"
            echo -e "${RED}[Request $id] Response: $response${NC}"
        fi
    else
        echo -e "${RED}[Request $id] ERROR: Failed to connect (took ${duration}s)${NC}"
    fi
    
    echo "$duration"
}

# Send a single request
echo -e "${CYAN}Sending single baseline request...${NC}"
send_request "Baseline" "50.00"

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# ====================================
# Test 2: Bulkhead Testing (10 concurrent requests)
# ====================================
echo -e "${BLUE}=== Test 2: Bulkhead Testing (10 concurrent requests) ===${NC}"
echo -e "${YELLOW}This test sends 10 concurrent requests to test bulkhead limits (5)${NC}"
echo -e "${YELLOW}Expected: 5 should be processed, others should be rejected or queued${NC}"
echo ""

# Function to generate a random amount between 10 and 200
random_amount() {
    echo "$(( (RANDOM % 190) + 10 )).99"
}

# Send concurrent requests to test bulkhead
echo -e "${CYAN}Sending 10 concurrent requests...${NC}"
pids=()
results=()

for i in {1..10}; do
    amount=$(random_amount)
    echo -e "${PURPLE}[Request $i/10] Initiating payment request for \$$amount...${NC}"
    send_request "$i" "$amount" > /tmp/bulkhead_result_$i.txt &
    pids+=($!)
done

# Wait for all requests to complete
for pid in "${pids[@]}"; do
    wait $pid
done

# Display results in order (output was suppressed while running in background)
echo ""
echo -e "${BLUE}=== Concurrent Request Results ===${NC}"
for i in {1..10}; do
    cat /tmp/bulkhead_result_$i.txt
done

# Count results by grepping the captured output files
echo ""
echo -e "${BLUE}=== Results Summary ===${NC}"
success_count=0
rejected_count=0
fallback_count=0
error_count=0

for i in {1..10}; do
    if grep -q "SUCCESS" /tmp/bulkhead_result_$i.txt 2>/dev/null; then
        success_count=$((success_count + 1))
    elif grep -q "REJECTED" /tmp/bulkhead_result_$i.txt 2>/dev/null; then
        rejected_count=$((rejected_count + 1))
    elif grep -q "FALLBACK" /tmp/bulkhead_result_$i.txt 2>/dev/null; then
        fallback_count=$((fallback_count + 1))
    else
        error_count=$((error_count + 1))
    fi
    rm /tmp/bulkhead_result_$i.txt
done

echo -e "${CYAN}Successful requests: $success_count${NC}"
echo -e "${CYAN}Rejected requests (bulkhead full): $rejected_count${NC}"
echo -e "${CYAN}Fallback requests (retries exhausted): $fallback_count${NC}"
echo -e "${CYAN}Error requests: $error_count${NC}"

# ====================================
# Log Analysis
# ====================================
if [ -f "$LOG_FILE" ] && [ $LOG_POSITION -gt 0 ]; then
    echo ""
    echo -e "${BLUE}=== Server Log Analysis ===${NC}"
    
    # Extract relevant log entries
    echo -e "${CYAN}Latest log entries related to payment processing:${NC}"
    tail -n +$LOG_POSITION "$LOG_FILE" | grep -E "Processing payment for amount|Bulkhead|concurrent|reject" | head -20
fi

# ====================================
# Test 3: Sequential Requests (After Bulkhead Test)
# ====================================
echo ""
echo -e "${BLUE}=== Test 3: Sequential Requests After Bulkhead Test ===${NC}"
echo -e "${YELLOW}This test sends 3 sequential requests to verify service recovery${NC}"
echo -e "${YELLOW}Expected: All should succeed now that the bulkhead pressure is released${NC}"
echo ""

# Wait a moment for bulkhead to clear
echo -e "${CYAN}Waiting 5 seconds for bulkhead to clear...${NC}"
sleep 5

# Send 3 sequential requests
for i in {1..3}; do
    amount=$(random_amount)
    echo -e "${CYAN}Sending sequential request $i/3 (\$$amount)...${NC}"
    send_request "Sequential-$i" "$amount"
    echo ""
    sleep 1
done

# ====================================
# Summary and Conclusion
# ====================================
echo ""
echo -e "${BLUE}==============================================${NC}"
echo -e "${BLUE}     Bulkhead Testing Summary     ${NC}"
echo -e "${BLUE}==============================================${NC}"
echo ""

echo -e "${GREEN}=== Bulkhead Testing Complete ===${NC}"
echo ""
echo -e "${YELLOW}Key observations:${NC}"
echo -e "${CYAN}1. The PaymentService uses a bulkhead of 5 concurrent requests${NC}"
echo -e "${CYAN}2. Requests beyond the bulkhead limit are rejected with an error${NC}"
echo -e "${CYAN}3. Successful requests complete even under concurrent load${NC}"
echo -e "${CYAN}4. The service recovers quickly once load decreases${NC}"
echo -e "${CYAN}5. This protects the system from being overwhelmed${NC}"
echo ""
echo -e "${YELLOW}This demonstrates how the @Bulkhead annotation:${NC}"
echo -e "${CYAN}• Limits concurrent requests to prevent system overload${NC}"
echo -e "${CYAN}• Rejects excess requests instead of queuing them indefinitely${NC}"
echo -e "${CYAN}• Protects the system during traffic spikes${NC}"
echo -e "${CYAN}• Works with other fault tolerance annotations (@Retry, @Fallback, etc.)${NC}"
echo ""
echo -e "${YELLOW}For more details, see:${NC}"
echo -e "${CYAN}• MicroProfile Fault Tolerance Documentation${NC}"
echo -e "${CYAN}• https://download.eclipse.org/microprofile/microprofile-fault-tolerance-3.0/apidocs/org/eclipse/microprofile/faulttolerance/Bulkhead.html${NC}"
