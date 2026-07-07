#!/bin/bash

# Comprehensive load test for all fault tolerance patterns
# Tests retry, circuit breaker, bulkhead, and async under concurrent load

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
echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Payment Service Concurrent Load Test         ║${NC}"
echo -e "${BLUE}║  Testing All Fault Tolerance Patterns         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

# Test configuration
NUM_AUTHORIZE_REQUESTS=20
NUM_HEALTH_REQUESTS=15
NUM_NOTIFY_REQUESTS=15

echo -e "${YELLOW}Load Test Configuration:${NC}"
echo "  • Authorization requests (Retry): $NUM_AUTHORIZE_REQUESTS"
echo "  • Health check requests (Circuit Breaker): $NUM_HEALTH_REQUESTS"
echo "  • Notification requests (Async + Bulkhead): $NUM_NOTIFY_REQUESTS"
echo ""
echo -e "${CYAN}Expected Behavior:${NC}"
echo -e "${CYAN}  Phase 1 (Retry): Some failures will be retried and eventually succeed${NC}"
echo -e "${CYAN}  Phase 2 (Circuit Breaker): Circuit will OPEN after 50% failures, then recover${NC}"
echo -e "${CYAN}  Phase 3 (Bulkhead): Up to 30 concurrent requests accepted, excess triggers @Fallback${NC}"
echo ""

read -p "Press Enter to start the load test..."
echo ""

# Counters
declare -A success_count=( ["authorize"]=0 ["health"]=0 ["notify"]=0 )
declare -A failure_count=( ["authorize"]=0 ["health"]=0 ["notify"]=0 )
declare -A fallback_count=( ["authorize"]=0 ["health"]=0 ["notify"]=0 )

# Function to send authorization request
send_authorize_request() {
    local id=$1
    local amount=$((50 + RANDOM % 450))
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "${BASE_URL}/authorize?amount=${amount}" 2>/dev/null || echo "HTTP_STATUS:000")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q "fallback\|pending"; then
            echo -e "${YELLOW}[AUTH-$id] ⚡ Fallback${NC}"
            echo "fallback" > /tmp/auth_result_$id.txt
        else
            echo -e "${GREEN}[AUTH-$id] ✓ Success${NC}"
            echo "success" > /tmp/auth_result_$id.txt
        fi
    else
        echo -e "${RED}[AUTH-$id] ✗ Failed${NC}"
        echo "failed" > /tmp/auth_result_$id.txt
    fi
}

# Function to send health check request
send_health_request() {
    local id=$1
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X GET "${BASE_URL}/health/gateway" 2>/dev/null || echo "HTTP_STATUS:000")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q "healthy"; then
            echo -e "${GREEN}[HEALTH-$id] ✓ Healthy${NC}"
            echo "success" > /tmp/health_result_$id.txt
        else
            echo -e "${YELLOW}[HEALTH-$id] ⚠ Degraded${NC}"
            echo "fallback" > /tmp/health_result_$id.txt
        fi
    elif [ "$http_code" -eq 503 ]; then
        if echo "$body" | grep -q "circuit_open"; then
            echo -e "${PURPLE}[HEALTH-$id] ⚡ Circuit Breaker OPEN${NC}"
            echo "failed" > /tmp/health_result_$id.txt
        elif echo "$body" | grep -q "unhealthy"; then
            echo -e "${YELLOW}[HEALTH-$id] ⚠ Gateway Unhealthy${NC}"
            echo "fallback" > /tmp/health_result_$id.txt
        else
            echo -e "${RED}[HEALTH-$id] ✗ Service Unavailable${NC}"
            echo "failed" > /tmp/health_result_$id.txt
        fi
    else
        echo -e "${RED}[HEALTH-$id] ✗ Failed (HTTP $http_code)${NC}"
        echo "failed" > /tmp/health_result_$id.txt
    fi
}

# Function to send notification request
send_notify_request() {
    local id=$1
    local payment_id=$(printf "PAY-%05d" $id)
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "${BASE_URL}/notify/${payment_id}" 2>/dev/null || echo "HTTP_STATUS:000")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q "queued for retry"; then
            echo -e "${YELLOW}[NOTIFY-$id] ⚡ Fallback (bulkhead or timeout)${NC}"
            echo "fallback" > /tmp/notify_result_$id.txt
        else
            echo -e "${GREEN}[NOTIFY-$id] ✓ Sent${NC}"
            echo "success" > /tmp/notify_result_$id.txt
        fi
    else
        echo -e "${RED}[NOTIFY-$id] ✗ Failed (HTTP $http_code)${NC}"
        echo "failed" > /tmp/notify_result_$id.txt
    fi
}

# Phase 1: Concurrent authorization requests
echo -e "${BLUE}=== Phase 1: Authorization Load Test (Retry Pattern) ===${NC}"
echo -e "${CYAN}Sending $NUM_AUTHORIZE_REQUESTS concurrent authorization requests...${NC}"
echo ""

start_time=$(date +%s.%N)

for i in $(seq 1 $NUM_AUTHORIZE_REQUESTS); do
    send_authorize_request $i &
    sleep 0.05
done

wait

end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)
duration_formatted=$(printf "%.2f" $duration)

# Collect results from temp files
for i in $(seq 1 $NUM_AUTHORIZE_REQUESTS); do
    if [ -f "/tmp/auth_result_$i.txt" ]; then
        result=$(cat /tmp/auth_result_$i.txt)
        case $result in
            success) ((success_count["authorize"]++)) ;;
            fallback) ((fallback_count["authorize"]++)) ;;
            failed) ((failure_count["authorize"]++)) ;;
        esac
        rm /tmp/auth_result_$i.txt
    fi
done

echo ""
echo -e "${PURPLE}Authorization Phase Complete: ${duration_formatted}s${NC}"
echo -e "${GREEN}✓ Success: ${success_count["authorize"]}${NC}"
echo -e "${YELLOW}⚡ Fallback: ${fallback_count["authorize"]}${NC}"
echo -e "${RED}✗ Failed: ${failure_count["authorize"]}${NC}"
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

sleep 2

# Phase 2: Health check requests (circuit breaker)
echo -e "${BLUE}=== Phase 2: Health Check Load Test (Circuit Breaker) ===${NC}"
echo -e "${CYAN}Sending $NUM_HEALTH_REQUESTS health check requests (sequential to observe circuit breaker)...${NC}"
echo -e "${CYAN}Watch for circuit breaker to OPEN after failures, then potentially CLOSE after recovery${NC}"
echo ""

start_time=$(date +%s.%N)

# Send requests sequentially to better observe circuit breaker behavior
for i in $(seq 1 $NUM_HEALTH_REQUESTS); do
    send_health_request $i
    sleep 0.5  # Small delay to observe circuit breaker state changes
done

end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)
duration_formatted=$(printf "%.2f" $duration)

# Collect results from temp files
for i in $(seq 1 $NUM_HEALTH_REQUESTS); do
    if [ -f "/tmp/health_result_$i.txt" ]; then
        result=$(cat /tmp/health_result_$i.txt)
        case $result in
            success) ((success_count["health"]++)) ;;
            fallback) ((fallback_count["health"]++)) ;;
            failed) ((failure_count["health"]++)) ;;
        esac
        rm /tmp/health_result_$i.txt
    fi
done

echo ""
echo -e "${PURPLE}Health Check Phase Complete: ${duration_formatted}s${NC}"
echo -e "${GREEN}✓ Healthy: ${success_count["health"]}${NC}"
echo -e "${YELLOW}⚠ Degraded/Unhealthy: ${fallback_count["health"]}${NC}"
echo -e "${RED}✗ Failed/Circuit Open: ${failure_count["health"]}${NC}"
echo ""
echo -e "${CYAN}Circuit Breaker Behavior:${NC}"
echo -e "${CYAN}• Opens at 50% failure rate (after 4 requests)${NC}"
echo -e "${CYAN}• Simulated gateway has 60% failure rate${NC}"
echo -e "${CYAN}• Circuit stays open for 5 seconds, then tries to recover${NC}"
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

sleep 2

# Phase 3: Notification requests (bulkhead + async)
echo -e "${BLUE}=== Phase 3: Notification Load Test (Bulkhead + Async) ===${NC}"
echo -e "${CYAN}Sending $NUM_NOTIFY_REQUESTS concurrent notification requests...${NC}"
echo ""

start_time=$(date +%s.%N)

for i in $(seq 1 $NUM_NOTIFY_REQUESTS); do
    send_notify_request $i &
    sleep 0.05
done

wait

end_time=$(date +%s.%N)
duration=$(echo "$end_time - $start_time" | bc)
duration_formatted=$(printf "%.2f" $duration)

# Collect results from temp files
for i in $(seq 1 $NUM_NOTIFY_REQUESTS); do
    if [ -f "/tmp/notify_result_$i.txt" ]; then
        result=$(cat /tmp/notify_result_$i.txt)
        case $result in
            success) ((success_count["notify"]++)) ;;
            fallback) ((fallback_count["notify"]++)) ;;
            failed) ((failure_count["notify"]++)) ;;
        esac
        rm /tmp/notify_result_$i.txt
    fi
done

echo ""
echo -e "${PURPLE}Notification Phase Complete: ${duration_formatted}s${NC}"
echo -e "${GREEN}✓ Queued: ${success_count["notify"]}${NC}"
echo -e "${YELLOW}⚡ Fallback: ${fallback_count["notify"]}${NC}"
echo -e "${RED}✗ Rejected: ${failure_count["notify"]}${NC}"
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Summary
total_requests=$((NUM_AUTHORIZE_REQUESTS + NUM_HEALTH_REQUESTS + NUM_NOTIFY_REQUESTS))
total_success=$((success_count["authorize"] + success_count["health"] + success_count["notify"]))
total_fallback=$((fallback_count["authorize"] + fallback_count["health"] + fallback_count["notify"]))
total_failure=$((failure_count["authorize"] + failure_count["health"] + failure_count["notify"]))

echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║              Load Test Summary                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Total Requests: ${total_requests}${NC}"
echo -e "${GREEN}✓ Successful: ${total_success}${NC}"
echo -e "${YELLOW}⚡ Fallback: ${total_fallback}${NC}"
echo -e "${RED}✗ Failed: ${total_failure}${NC}"
echo ""

success_rate=$(echo "scale=2; ($total_success * 100) / $total_requests" | bc)
echo -e "${CYAN}Success Rate: ${success_rate}%${NC}"
echo ""

# Fetch and display metrics
echo -e "${BLUE}=== Fault Tolerance Metrics Summary ===${NC}"
echo ""

echo -e "${YELLOW}Retry Metrics (authorizePayment):${NC}"
retry_metrics=$(curl -s "$METRICS_URL" 2>/dev/null | grep -i "retry.*authorizePayment\|authorizePayment.*retry")
if [ -n "$retry_metrics" ]; then
    echo "$retry_metrics" | head -10
else
    echo -e "${CYAN}No retry metrics found. Checking alternative patterns...${NC}"
    curl -s "$METRICS_URL" 2>/dev/null | grep -iE "ft.*retry|retry.*total|retry.*calls" | head -5
fi

echo ""
echo -e "${YELLOW}Circuit Breaker Metrics (checkGatewayHealth):${NC}"
cb_metrics=$(curl -s "$METRICS_URL" 2>/dev/null | grep -i "circuitbreaker.*checkGatewayHealth\|checkGatewayHealth.*circuit")
if [ -n "$cb_metrics" ]; then
    echo "$cb_metrics" | head -10
else
    echo -e "${CYAN}No circuit breaker metrics found. Checking alternative patterns...${NC}"
    curl -s "$METRICS_URL" 2>/dev/null | grep -iE "ft.*circuit|circuit.*state|circuit.*calls" | head -5
fi

echo ""
echo -e "${YELLOW}Bulkhead Metrics (sendPaymentNotification):${NC}"
bulkhead_metrics=$(curl -s "$METRICS_URL" 2>/dev/null | grep -i "bulkhead.*sendPaymentNotification\|sendPaymentNotification.*bulkhead")
if [ -n "$bulkhead_metrics" ]; then
    echo "$bulkhead_metrics" | head -10
else
    echo -e "${CYAN}No bulkhead metrics found. Checking alternative patterns...${NC}"
    curl -s "$METRICS_URL" 2>/dev/null | grep -iE "ft.*bulkhead|bulkhead.*accept|bulkhead.*reject" | head -5
fi

echo ""
echo -e "${GREEN}=== Load Test Complete ===${NC}"
echo ""
echo -e "${CYAN}To view all fault tolerance metrics:${NC}"
echo -e "${BLUE}curl $METRICS_URL | grep -i ft${NC}"
echo ""
