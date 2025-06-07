#!/bin/bash

# Test script specifically for demonstrating retry behavior
# This script sends multiple requests with a negative amount to ensure failures
# and observe the retry mechanism in action

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

echo -e "${BLUE}=== Testing Retry Mechanism ====${NC}"
echo -e "${CYAN}This test will force failures to demonstrate the retry mechanism${NC}"
echo ""

# Monitor the server logs in real-time
echo -e "${YELLOW}Starting log monitor in background...${NC}"
LOG_FILE="/workspaces/liberty-rest-app/payment/target/liberty/wlp/usr/servers/mpServer/logs/messages.log"

# Get initial log position
if [ -f "$LOG_FILE" ]; then
    LOG_POSITION=$(wc -l < "$LOG_FILE")
else
    LOG_POSITION=0
    echo -e "${RED}Log file not found. Will attempt to continue.${NC}"
fi

# Send a request that will trigger the random failure condition (30% success rate)
echo -e "${YELLOW}Sending requests that will likely trigger retries...${NC}"
echo -e "${CYAN}(Our code has a 70% chance of failure, which should trigger retries)${NC}"

# Send multiple requests to increase chance of seeing retry behavior
for i in {1..5}; do
    echo -e "${PURPLE}[Request $i] Sending request...${NC}"
    response=$(curl -s -X POST "${PAYMENT_URL}?amount=19.99")
    
    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}[Request $i] SUCCESS: $response${NC}"
    else
        echo -e "${RED}[Request $i] FALLBACK: $response${NC}"
    fi
    
    # Brief pause between requests
    sleep 1
done

# Wait a moment for retries to complete
echo -e "${YELLOW}Waiting for retries to complete...${NC}"
sleep 10

# Display relevant log entries
echo ""
echo -e "${BLUE}=== Log Analysis ====${NC}"
if [ -f "$LOG_FILE" ]; then
    echo -e "${CYAN}Extracting relevant log entries:${NC}"
    echo ""
    
    # Extract and display new log entries related to payment processing
    NEW_LOGS=$(tail -n +$LOG_POSITION "$LOG_FILE" | grep -E "Processing payment|Fallback invoked|PaymentProcessingException|Retry|Timeout")
    
    if [ -n "$NEW_LOGS" ]; then
        echo "$NEW_LOGS"
    else
        echo -e "${RED}No relevant log entries found.${NC}"
    fi
else
    echo -e "${RED}Log file not found${NC}"
fi

echo ""
echo -e "${BLUE}=== Retry Behavior Analysis ====${NC}"
echo -e "${CYAN}1. Look for multiple 'Processing payment' entries with the same amount${NC}"
echo -e "${CYAN}2. 'PaymentProcessingException' indicates a failure that should trigger retry${NC}"
echo -e "${CYAN}3. After max retries (3), the fallback method is called${NC}"
echo -e "${CYAN}4. Note the time delays between retries (2000ms + jitter)${NC}"
