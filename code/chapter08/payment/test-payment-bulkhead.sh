#!/bin/bash

# Test script for Payment Service Bulkhead functionality
# Tests the @Bulkhead annotation on the sendPaymentNotification method

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Check if bc is installed
if ! command -v bc &> /dev/null; then
    echo -e "${YELLOW}Installing bc for duration calculations...${NC}"
    sudo apt-get update && sudo apt-get install -y bc
fi

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    BASE_URL="http://localhost:9080/payment/api"
    METRICS_URL="http://localhost:9080/metrics"
    echo -e "${CYAN}Detected GitHub Codespaces environment${NC}"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    METRICS_URL="https://9080-$GITPOD_HOST/metrics"
    echo -e "${CYAN}Detected Gitpod environment${NC}"
else
    BASE_URL="http://localhost:9080/payment/api"
    METRICS_URL="http://localhost:9080/metrics"
    echo -e "${CYAN}Using local environment${NC}"
fi

echo ""
echo -e "${BLUE}=== Payment Notification Bulkhead Test ===${NC}"
echo -e "${CYAN}Endpoint: POST /notify/{paymentId}${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

echo -e "${YELLOW}Bulkhead Configuration (@Bulkhead on sendPaymentNotification):${NC}"
echo "  • Maximum Concurrent Requests: 10  (value = 10)"
echo "  • Waiting Queue Size:          20  (waitingTaskQueue = 20)"
echo "  • Total Capacity:              30  (10 concurrent + 20 queued)"
echo "  • Asynchronous:                Yes (@Asynchronous)"
echo "  • Timeout per request:         7000ms"
echo ""

echo -e "${CYAN}Expected Behavior:${NC}"
echo "  • Requests 1–30:  Accepted (concurrent slots + waiting queue)"
echo "  • Requests 31+:   BulkheadException → caught by @Fallback → 'Notification queued for retry'"
echo ""

# Function to send a single async notification request
send_notification() {
    local id=$1
    local payment_id="PAY-$(printf "%05d" $id)"

    start_time=$(date +%s.%N)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST \
        "${BASE_URL}/notify/${payment_id}" 2>/dev/null || echo "HTTP_STATUS:000")
    end_time=$(date +%s.%N)

    duration=$(echo "$end_time - $start_time" | bc)
    duration_formatted=$(printf "%.3f" $duration)

    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')

    if [ "$http_code" -eq 202 ]; then
        if echo "$body" | grep -qi "queued for retry"; then
            echo -e "${YELLOW}[Request $id] ⚡ Bulkhead full → Fallback (HTTP 202, ${duration_formatted}s) — $body${NC}"
        else
            echo -e "${GREEN}[Request $id] ✓ Success (HTTP 202, ${duration_formatted}s) — $body${NC}"
        fi
    elif [ "$http_code" -eq 503 ]; then
        echo -e "${YELLOW}[Request $id] ⚠ Service Unavailable (HTTP 503, ${duration_formatted}s) — $body${NC}"
    elif [ "$http_code" -eq 000 ]; then
        echo -e "${RED}[Request $id] ✗ Connection failed — is the service running on port 9080?${NC}"
    else
        echo -e "${PURPLE}[Request $id] ? HTTP $http_code (${duration_formatted}s): $body${NC}"
    fi
}

# ====================================
# Phase 1: Single Request (Baseline)
# ====================================
echo -e "${BLUE}=== Phase 1: Single Request (Baseline) ===${NC}"
echo -e "${CYAN}Sending one request to confirm the endpoint is healthy...${NC}"
echo ""

send_notification 1
sleep 1

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# ====================================
# Phase 2: Concurrent Load Within Capacity
# ====================================
echo -e "${BLUE}=== Phase 2: 10 Concurrent Requests (Within Capacity) ===${NC}"
echo -e "${CYAN}All 10 should be accepted (fills the 10 concurrent slots).${NC}"
echo ""

for i in {1..10}; do
    send_notification $i &
done
wait

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# ====================================
# Phase 3: Saturate the Bulkhead
# ====================================
echo -e "${BLUE}=== Phase 3: 35 Concurrent Requests (Exceeds Capacity of 30) ===${NC}"
echo -e "${CYAN}Sending 35 requests simultaneously...${NC}"
echo -e "${YELLOW}Expected:${NC}"
echo -e "  • Requests 1–30:  ${GREEN}Success — normal execution${NC}"
echo -e "  • Requests 31–35: ${YELLOW}Fallback — bulkhead full, BulkheadException caught by @Fallback${NC}"
echo ""

for i in {1..35}; do
    send_notification $i &
done
wait

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# ====================================
# Phase 4: Recovery After Load
# ====================================
echo -e "${BLUE}=== Phase 4: Recovery (Sequential Requests After Load) ===${NC}"
echo -e "${CYAN}Sending 3 sequential requests — all should succeed now that load has cleared...${NC}"
echo ""

for i in {101..103}; do
    send_notification $i
    sleep 0.5
done

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# ====================================
# Phase 5: Bulkhead Metrics
# ====================================
echo -e "${BLUE}=== Phase 5: Bulkhead Metrics ===${NC}"
echo -e "${CYAN}Querying fault tolerance metrics from: $METRICS_URL${NC}"
echo ""

metrics=$(curl -s "$METRICS_URL" 2>/dev/null | grep -i "bulkhead.*sendPaymentNotification\|ft.*bulkhead")

if [ -n "$metrics" ]; then
    echo "$metrics" | while IFS= read -r line; do
        if echo "$line" | grep -qi "accepted\|running"; then
            echo -e "${GREEN}  $line${NC}"
        elif echo "$line" | grep -qi "rejected"; then
            echo -e "${RED}  $line${NC}"
        else
            echo -e "${CYAN}  $line${NC}"
        fi
    done
else
    echo -e "${YELLOW}No bulkhead metrics found.${NC}"
    echo -e "${CYAN}Tip: metrics are exposed at $METRICS_URL after requests have been processed.${NC}"
fi

echo ""
echo -e "${GREEN}=== Bulkhead Test Complete ===${NC}"
echo ""
echo -e "${CYAN}Summary:${NC}"
echo "  • @Bulkhead(value=10, waitingTaskQueue=20) limits concurrent async executions"
echo "  • Requests beyond total capacity (30) throw BulkheadException"
echo "  • @Fallback catches BulkheadException and returns a degraded 'queued for retry' response"
echo "  • @Asynchronous + @Bulkhead together provide non-blocking, resource-isolated processing"
echo ""
echo -e "${CYAN}To view bulkhead metrics directly:${NC}"
echo -e "  ${BLUE}curl $METRICS_URL | grep -i bulkhead${NC}"
echo ""
