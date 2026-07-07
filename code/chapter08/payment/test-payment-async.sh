#!/bin/bash

# Test script for asynchronous payment notification processing
# Demonstrates @Asynchronous annotation behavior

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
    echo -e "${YELLOW}Installing bc...${NC}"
    sudo apt-get update && sudo apt-get install -y bc
fi

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
echo -e "${BLUE}=== Testing Asynchronous Payment Notifications ===${NC}"
echo -e "${CYAN}Endpoint: POST /notify/{paymentId}${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

echo -e "${YELLOW}Asynchronous Configuration:${NC}"
echo "  • Method: sendPaymentNotification()"
echo "  • Return Type: CompletionStage<String>"
echo "  • Processing: Non-blocking"
echo "  • Simulated delay: ~2 seconds"
echo ""

echo -e "${CYAN}🔍 Testing Goals:${NC}"
echo "  1. Show that a single request still takes ~2s — @Asynchronous doesn't"
echo "     shorten one request's response time, it frees the server thread"
echo "  2. Demonstrate concurrent requests run in parallel, not one at a time"
echo "  3. Show CompletionStage behavior enabling Bulkhead-based concurrency"
echo ""

# Function to send notification and measure response time
send_async_notification() {
    local id=$1
    local payment_id=$2
    
    start_time=$(date +%s.%N)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "${BASE_URL}/notify/${payment_id}" 2>/dev/null || echo "HTTP_STATUS:000")
    end_time=$(date +%s.%N)

    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    wall_time=$(echo "$end_time - $start_time" | bc)
    wall_time_formatted=$(printf "%.3f" $wall_time)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        # Note: a single request always takes ~2s (the simulated notification
        # work) regardless of @Asynchronous — the client only gets a response
        # once that work finishes. This is not a pass/fail check.
        echo -e "${GREEN}[Request $id] ✓ Completed in ${wall_time_formatted}s${NC}"
        echo -e "${GREEN}            Response: $body${NC}"
    else
        echo -e "${RED}[Request $id] ✗ ERROR (HTTP $http_code, ${wall_time_formatted}s)${NC}"
        echo -e "${RED}            Response: $body${NC}"
    fi
    
    echo "$wall_time_formatted"
}

# Test 1: Single request baseline
echo -e "${BLUE}=== Test 1: Single Request (Baseline Latency) ===${NC}"
echo -e "${CYAN}Sending a single notification to measure round-trip latency...${NC}"
echo -e "${YELLOW}Expected: ~2s. @Asynchronous does not shorten this — the client${NC}"
echo -e "${YELLOW}always waits for the notification work to finish before getting a response.${NC}"
echo ""

send_async_notification 1 "PAY-00001"

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Test 2: Multiple sequential requests
echo -e "${BLUE}=== Test 2: Sequential Requests (Baseline Comparison) ===${NC}"
echo -e "${CYAN}Sending 5 requests one after another from this single client...${NC}"
echo -e "${YELLOW}Expected: ~2s per request, ~10s total. Sequential calls don't benefit${NC}"
echo -e "${YELLOW}from async — this client waits for each response before sending the next,${NC}"
echo -e "${YELLOW}so it's a baseline to contrast with Test 3's concurrent requests.${NC}"
echo ""

total_start=$(date +%s.%N)

for i in {2..6}; do
    payment_id=$(printf "PAY-%05d" $i)
    send_async_notification $i $payment_id
done

total_end=$(date +%s.%N)
total_time=$(echo "$total_end - $total_start" | bc)
total_time_formatted=$(printf "%.2f" $total_time)

echo ""
echo -e "${PURPLE}Total time for 5 sequential requests: ${total_time_formatted}s${NC}"
echo -e "${CYAN}Expected: ~10s (5 × ~2s) — this is normal for sequential calls and${NC}"
echo -e "${CYAN}does not indicate blocking. Compare against Test 3's concurrent total.${NC}"

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Test 3: Concurrent requests
echo -e "${BLUE}=== Test 3: Concurrent Requests (True Async Test) ===${NC}"
echo -e "${CYAN}Sending 5 concurrent requests in parallel...${NC}"
echo -e "${YELLOW}This is where @Asynchronous + @Bulkhead actually pay off: the server${NC}"
echo -e "${YELLOW}runs all 5 notifications in parallel on background threads instead of${NC}"
echo -e "${YELLOW}queueing them one at a time, so the total should be ~2s, not ~10s.${NC}"
echo ""

concurrent_start=$(date +%s.%N)

# Launch background jobs
for i in {7..11}; do
    payment_id=$(printf "PAY-%05d" $i)
    (send_async_notification $i $payment_id) &
    sleep 0.05  # Small stagger to make output readable
done

# Wait for all background jobs
wait

concurrent_end=$(date +%s.%N)
concurrent_time=$(echo "$concurrent_end - $concurrent_start" | bc)
concurrent_time_formatted=$(printf "%.2f" $concurrent_time)

echo ""
echo -e "${PURPLE}Total time for 5 concurrent requests: ${concurrent_time_formatted}s${NC}"
echo -e "${CYAN}Expected: ~2-3s — all 5 notifications run in parallel, so total time${NC}"
echo -e "${CYAN}tracks one notification's duration, not the sum of all five.${NC}"

if (( $(echo "$concurrent_time < $total_time / 2" | bc -l) )); then
    echo -e "${GREEN}✓ CONFIRMED: Concurrent total (${concurrent_time_formatted}s) is far below the${NC}"
    echo -e "${GREEN}  sequential total (${total_time_formatted}s) — notifications ran in parallel.${NC}"
else
    echo -e "${YELLOW}⚠ WARNING: Concurrent total is close to the sequential total — check that${NC}"
    echo -e "${YELLOW}  @Asynchronous/@Bulkhead are active and the bulkhead isn't saturated.${NC}"
fi

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Summary
echo -e "${BLUE}=== Asynchronous Processing Summary ===${NC}"
echo ""
echo -e "${CYAN}Key Observations:${NC}"
echo "  • @Asynchronous methods return CompletionStage, run on a background thread pool"
echo "  • JAX-RS endpoint returns CompletionStage<Response>, completing the HTTP"
echo "    response only once that background work finishes — a single client still"
echo "    waits the full ~2s; @Asynchronous does not make one request faster"
echo "  • What it does buy: the request-handling thread isn't blocked while waiting,"
echo "    so the server can process many notifications concurrently instead of"
echo "    queueing them one at a time (see Test 3 vs Test 2)"
echo "  • @Bulkhead caps how many of these background notifications run at once"
echo ""

echo -e "${CYAN}CompletionStage vs Future:${NC}"
echo "  • CompletionStage: Fault tolerance applied, supports chaining"
echo "  • Future: Fault tolerance NOT applied (avoid!)"
echo ""

echo -e "${GREEN}=== Test Complete ===${NC}"
echo ""
