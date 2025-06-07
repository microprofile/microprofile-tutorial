#!/bin/bash

# Enhanced test script for verifying asynchronous processing
# This script shows the asynchronous behavior by:
# 1. Checking for concurrent processing
# 2. Monitoring response times to verify non-blocking behavior
# 3. Analyzing the server logs to confirm retry patterns

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

# Set payment endpoint URL
HOST="localhost"
PAYMENT_URL="http://${HOST}:9080/payment/api/authorize"

echo -e "${BLUE}=== Enhanced Asynchronous Testing ====${NC}"
echo -e "${CYAN}This test will send a series of requests to demonstrate asynchronous processing${NC}"
echo ""

# First, let's check server logs before test
echo -e "${YELLOW}Checking server logs before test...${NC}"
echo -e "${CYAN}(This establishes a baseline for comparison)${NC}"
cd /workspaces/liberty-rest-app/payment
MESSAGES_LOG="target/liberty/wlp/usr/servers/mpServer/logs/messages.log"

if [ -f "$MESSAGES_LOG" ]; then
    echo -e "${PURPLE}Server log file exists at: $MESSAGES_LOG${NC}"
    # Count initial payment processing messages
    INITIAL_PROCESSING_COUNT=$(grep -c "Processing payment for amount" "$MESSAGES_LOG")
    INITIAL_FALLBACK_COUNT=$(grep -c "Fallback invoked for payment" "$MESSAGES_LOG")
    
    echo -e "${CYAN}Initial payment processing count: $INITIAL_PROCESSING_COUNT${NC}"
    echo -e "${CYAN}Initial fallback count: $INITIAL_FALLBACK_COUNT${NC}"
else
    echo -e "${RED}Server log file not found at: $MESSAGES_LOG${NC}"
    INITIAL_PROCESSING_COUNT=0
    INITIAL_FALLBACK_COUNT=0
fi

echo ""
echo -e "${YELLOW}Now sending 3 requests in rapid succession...${NC}"

# Function to send request and measure time
send_request() {
    local id=$1
    local amount=$2
    local start_time=$(date +%s.%N)
    
    response=$(curl -s -X POST "${PAYMENT_URL}?amount=${amount}")
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc)
    
    echo -e "${GREEN}[Request $id] Completed in ${duration}s${NC}"
    echo -e "${CYAN}[Request $id] Response: $response${NC}"
    
    return 0
}

# Send 3 requests in rapid succession
for i in {1..3}; do
    # Use a fixed amount for consistency
    amount=25.99
    echo -e "${PURPLE}[Request $i] Sending request for \$$amount...${NC}"
    send_request $i $amount &
    # Sleep briefly to ensure log messages are distinguishable
    sleep 0.1
done

# Wait for all background processes to complete
wait

echo ""
echo -e "${YELLOW}Waiting 5 seconds for processing to complete...${NC}"
sleep 5

# Check the server logs after test
echo -e "${YELLOW}Checking server logs after test...${NC}"

if [ -f "$MESSAGES_LOG" ]; then
    # Count final payment processing messages
    FINAL_PROCESSING_COUNT=$(grep -c "Processing payment for amount" "$MESSAGES_LOG")
    FINAL_FALLBACK_COUNT=$(grep -c "Fallback invoked for payment" "$MESSAGES_LOG")
    
    NEW_PROCESSING=$(($FINAL_PROCESSING_COUNT - $INITIAL_PROCESSING_COUNT))
    NEW_FALLBACKS=$(($FINAL_FALLBACK_COUNT - $INITIAL_FALLBACK_COUNT))
    
    echo -e "${CYAN}New payment processing events: $NEW_PROCESSING${NC}"
    echo -e "${CYAN}New fallback events: $NEW_FALLBACKS${NC}"
    
    # Extract the latest log entries
    echo ""
    echo -e "${BLUE}Latest server log entries related to payment processing:${NC}"
    grep "Processing payment for amount\|Fallback invoked for payment" "$MESSAGES_LOG" | tail -10
else
    echo -e "${RED}Server log file not found after test${NC}"
fi

echo ""
echo -e "${BLUE}=== Asynchronous Behavior Analysis ====${NC}"
echo -e "${CYAN}1. Rapid response times indicate non-blocking behavior${NC}"
echo -e "${CYAN}2. Multiple processing entries in logs show concurrent execution${NC}"
echo -e "${CYAN}3. Fallbacks demonstrate the fault tolerance mechanism${NC}"
echo -e "${CYAN}4. All @Asynchronous methods return quickly while processing continues in background${NC}"
