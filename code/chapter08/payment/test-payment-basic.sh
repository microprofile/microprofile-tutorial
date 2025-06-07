#!/bin/bash

# Simple test script for payment service

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

HOST="localhost"
PAYMENT_URL="http://${HOST}:9080/payment/api/authorize"

# Hardcoded amount for testing
AMOUNT=25.99

echo -e "${YELLOW}Sending payment request for \$$AMOUNT...${NC}"
echo ""

# Capture start time
start_time=$(date +%s.%N)

# Send request
response=$(curl -s -X POST "${PAYMENT_URL}?amount=${AMOUNT}")

# Capture end time
end_time=$(date +%s.%N)

# Calculate duration
duration=$(echo "$end_time - $start_time" | bc)

echo ""
echo -e "${GREEN}✓ Request completed in ${duration} seconds${NC}"
echo -e "${YELLOW}Response:${NC} $response"
echo ""

# Show if the response indicates success or fallback
if echo "$response" | grep -q "success"; then
    echo -e "${GREEN}✓ SUCCESS: Payment processed successfully${NC}"
elif echo "$response" | grep -q "failed"; then
    echo -e "${RED}✗ FALLBACK: Payment service unavailable${NC}"
else
    echo -e "${RED}✗ ERROR: Unexpected response${NC}"
fi
