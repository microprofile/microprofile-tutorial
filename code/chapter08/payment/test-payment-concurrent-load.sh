#!/bin/bash

# Test script for asynchronous payment processing
# This script sends multiple concurrent requests to test:
# 1. Asynchronous processing (@Asynchronous)
# 2. Resource isolation (@Bulkhead)
# 3. Retry behavior (@Retry)

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

# Set payment endpoint URL - automatically detect if running in GitHub Codespaces
if [ -n "$CODESPACES" ]; then
    HOST="localhost"
else
    HOST="localhost"
fi

PAYMENT_URL="http://${HOST}:9080/payment/api/authorize"
NUM_REQUESTS=10
CONCURRENCY=5

echo -e "${BLUE}=== Testing Asynchronous Payment Processing ===${NC}"
echo -e "${CYAN}Endpoint: ${PAYMENT_URL}${NC}"
echo -e "${CYAN}Sending ${NUM_REQUESTS} requests with concurrency ${CONCURRENCY}${NC}"
echo ""

# Function to send a single request and capture timing
send_request() {
    local id=$1
    local amount=$2
    local start_time=$(date +%s)
    
    echo -e "${YELLOW}[Request $id] Sending payment request for \$$amount...${NC}"
    
    # Send request and capture response
    response=$(curl -s -X POST "${PAYMENT_URL}?amount=${amount}")
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    # Check if response contains "success" or "failed"
    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}[Request $id] SUCCESS: Payment processed in ${duration}s${NC}"
        echo -e "${GREEN}[Request $id] Response: $response${NC}"
    elif echo "$response" | grep -q "failed"; then
        echo -e "${RED}[Request $id] FALLBACK: Service unavailable (took ${duration}s)${NC}"
        echo -e "${RED}[Request $id] Response: $response${NC}"
    else
        echo -e "${RED}[Request $id] ERROR: Unexpected response (took ${duration}s)${NC}"
        echo -e "${RED}[Request $id] Response: $response${NC}"
    fi
    
    # Return the duration for analysis
    echo "$duration"
}

# Run concurrent requests using GNU Parallel if available, or background processes if not
if command -v parallel > /dev/null; then
    echo -e "${PURPLE}Using GNU Parallel for concurrent requests${NC}"
    export -f send_request
    export PAYMENT_URL RED GREEN YELLOW BLUE PURPLE CYAN NC
    
    # Use predefined amounts instead of bc calculation
    amounts=("15.99" "24.50" "19.95" "32.75" "12.99" "22.50" "18.75" "29.99" "14.50" "27.25")
    for i in $(seq 1 $NUM_REQUESTS); do
        amount_index=$((i-1 % 10))
        amount=${amounts[$amount_index]}
        send_request $i $amount &
    done
else
    echo -e "${PURPLE}Running concurrent requests using background processes${NC}"
    # Store PIDs
    pids=()
    
    # Predefined amounts
    amounts=("15.99" "24.50" "19.95" "32.75" "12.99" "22.50" "18.75" "29.99" "14.50" "27.25")
    
    # Launch requests in background
    for i in $(seq 1 $NUM_REQUESTS); do
        # Get amount from predefined list
        amount_index=$((i-1 % 10))
        amount=${amounts[$amount_index]}
        send_request $i $amount &
        pids+=($!)
        
        # Control concurrency
        if [ ${#pids[@]} -ge $CONCURRENCY ]; then
            # Wait for one process to finish before starting more
            wait "${pids[0]}"
            pids=("${pids[@]:1}")
        fi
    done
    
    # Wait for remaining processes
    for pid in "${pids[@]}"; do
        wait $pid
    done
fi

echo ""
echo -e "${BLUE}=== Test Complete ===${NC}"
echo -e "${CYAN}Analyze the responses to verify:${NC}"
echo -e "${CYAN}1. Asynchronous processing (@Asynchronous)${NC}"
echo -e "${CYAN}2. Resource isolation (@Bulkhead)${NC}"
echo -e "${CYAN}3. Retry behavior on failures (@Retry)${NC}"
