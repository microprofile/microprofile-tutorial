#!/bin/bash

# Test script for Payment Service Circuit Breaker functionality
# Tests the @CircuitBreaker annotation on the checkGatewayHealth method

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Dynamically determine the base URL
if [ -n "$CODESPACE_NAME" ] && [ -n "$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN" ]; then
    BASE_URL="http://localhost:9080/payment/api"
    METRICS_URL="http://localhost:9080/metrics?scope=base"
    echo -e "${CYAN}Detected GitHub Codespaces environment${NC}"
elif [ -n "$GITPOD_WORKSPACE_URL" ]; then
    GITPOD_HOST=$(echo $GITPOD_WORKSPACE_URL | sed 's|https://||' | sed 's|/||')
    BASE_URL="https://9080-$GITPOD_HOST/payment/api"
    METRICS_URL="https://9080-$GITPOD_HOST/metrics?scope=base"
    echo -e "${CYAN}Detected Gitpod environment${NC}"
else
    BASE_URL="http://localhost:9080/payment/api"
    METRICS_URL="http://localhost:9080/metrics?scope=base"
    echo -e "${CYAN}Using local environment${NC}"
fi

echo ""
echo -e "${BLUE}=== Payment Gateway Circuit Breaker Test ===${NC}"
echo -e "${CYAN}Endpoint: GET /health/gateway${NC}"
echo -e "${CYAN}Base URL: $BASE_URL${NC}"
echo ""

echo -e "${YELLOW}Circuit Breaker Configuration:${NC}"
echo "  • Request Volume Threshold: 4"
echo "  • Failure Ratio: 0.5 (50%)"
echo "  • Delay: 5000ms (OPEN → HALF_OPEN wait)"
echo "  • Success Threshold: 2 (consecutive successes to CLOSE from HALF_OPEN)"
echo "  • Timeout: 2000ms per health check attempt"
echo "  • Simulated failure rate: 40% (Math.random() > 0.6, 60% success rate)"
echo ""

echo -e "${CYAN}🔍 Circuit Breaker States:${NC}"
echo -e "  • ${GREEN}CLOSED${NC} (0): Normal operation, requests pass through"
echo -e "  • ${RED}OPEN${NC} (1): Too many failures, requests immediately fail"
echo -e "  • ${YELLOW}HALF_OPEN${NC} (2): Testing if service recovered"
echo ""

echo -e "${PURPLE}This test will:${NC}"
echo "  1. Send initial requests to establish baseline"
echo "  2. Trigger failures to trip the circuit breaker to OPEN"
echo "  3. Wait for delay period (5s)"
echo "  4. Test HALF_OPEN state (probing for recovery)"
echo "  5. Verify circuit returns to CLOSED on success"
echo ""

read -p "Press Enter to start the circuit breaker test..."
echo ""

# Function to check circuit breaker state from metrics
# Shows the raw ft_circuitbreaker_state_total cumulative nanosecond counters.
# These are cumulative — the counter that is currently growing indicates the active state.
# Compare values across calls: the one increasing between two readings is the current state.
check_circuit_state() {
    metrics=$(curl -s "$METRICS_URL" 2>/dev/null)
    state_metrics=$(echo "$metrics" | grep "ft_circuitbreaker_state_total" | grep "checkGatewayHealth")

    if [ -z "$state_metrics" ]; then
        if [ -z "$metrics" ]; then
            echo -e "${RED}✗ Unable to reach metrics endpoint${NC}"
            echo -e "${YELLOW}  Make sure the payment service is running on port 9080${NC}"
        else
            echo -e "${YELLOW}⚠ Circuit breaker metrics not yet available${NC}"
            echo -e "${CYAN}  (Normal before any health check requests have been made)${NC}"
        fi
        return
    fi

    closed_ns=$(echo "$state_metrics" | grep 'state="closed"'   | grep -o '[0-9.E+]*$' || echo "0")
    open_ns=$(echo "$state_metrics"   | grep 'state="open"'     | grep -o '[0-9.E+]*$' || echo "0")
    halfopen_ns=$(echo "$state_metrics" | grep 'state="halfOpen"' | grep -o '[0-9.E+]*$' || echo "0")

    echo -e "${CYAN}  ft_circuitbreaker_state_total (cumulative nanoseconds):${NC}"
    echo -e "    ${GREEN}CLOSED:    ${closed_ns} ns${NC}"
    echo -e "    ${RED}OPEN:      ${open_ns} ns${NC}"
    echo -e "    ${YELLOW}HALF_OPEN: ${halfopen_ns} ns${NC}"
    echo -e "${CYAN}  The counter currently increasing = the active state${NC}"
}

# Function to make health check request
make_health_request() {
    local request_num=$1
    
    start_time=$(date +%s.%N)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BASE_URL}/health/gateway" 2>/dev/null || echo "HTTP_STATUS:000")
    end_time=$(date +%s.%N)
    
    duration=$(echo "$end_time - $start_time" | bc)
    duration_formatted=$(printf "%.3f" $duration)
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if echo "$body" | grep -q "\"status\":\"healthy\""; then
        echo -e "${GREEN}[Request $request_num] ✓ Gateway healthy (${duration_formatted}s)${NC}"
    elif echo "$body" | grep -q "\"status\":\"unhealthy\""; then
        echo -e "${YELLOW}[Request $request_num] ⚠ Gateway unhealthy (${duration_formatted}s)${NC}"
    elif echo "$body" | grep -q "\"status\":\"circuit_open\""; then
        echo -e "${RED}[Request $request_num] ✗ Circuit OPEN - Request blocked (${duration_formatted}s)${NC}"
    else
        echo -e "${PURPLE}[Request $request_num] ? Unknown status (${duration_formatted}s): $body${NC}"
    fi
}

# Phase 0: Ensure circuit is CLOSED before starting test
echo -e "${BLUE}=== Phase 0: Initialization ===${NC}"
echo -e "${CYAN}Checking current circuit breaker state...${NC}"
echo ""

# Check initial state and wait if needed
metrics=$(curl -s "$METRICS_URL" 2>/dev/null)
state_metrics=$(echo "$metrics" | grep "ft_circuitbreaker_state_total" | grep "checkGatewayHealth")

if [ -z "$state_metrics" ]; then
    echo -e "${YELLOW}⚠ Circuit breaker metrics not yet available${NC}"
    echo -e "${CYAN}Sending initial requests to initialize circuit breaker...${NC}"
    # Send a few requests to initialize the circuit breaker
    for i in {1..2}; do
        curl -s "${BASE_URL}/health/gateway" > /dev/null 2>&1
        sleep 0.5
    done
    sleep 1
    echo -e "${GREEN}✓ Circuit breaker initialized${NC}"
elif echo "$state_metrics" | grep -q 'state="open"'; then
    # Check if there's significant open time (circuit is OPEN)
    open_time=$(echo "$state_metrics" | grep 'state="open"' | grep -o '[0-9.E+]*$')
    if [ -n "$open_time" ] && [ "$open_time" != "0" ]; then
        echo -e "${YELLOW}⚠ Circuit is currently OPEN (from previous test run)${NC}"
        echo -e "${CYAN}Waiting 6 seconds for circuit to transition to HALF_OPEN...${NC}"
        for i in {6..1}; do
            echo -ne "${CYAN}  Waiting... $i seconds\r${NC}"
            sleep 1
        done
        echo ""
        echo -e "${CYAN}Sending probe requests to close the circuit...${NC}"
        # Send a few successful probes to close the circuit
        for i in {1..3}; do
            curl -s "${BASE_URL}/health/gateway" > /dev/null 2>&1
            sleep 0.5
        done
        sleep 2
        echo -e "${GREEN}✓ Circuit should now be CLOSED${NC}"
    fi
elif echo "$state_metrics" | grep -q 'state="halfOpen"'; then
    halfopen_time=$(echo "$state_metrics" | grep 'state="halfOpen"' | grep -o '[0-9.E+]*$')
    if [ -n "$halfopen_time" ] && [ "$halfopen_time" != "0" ]; then
        echo -e "${YELLOW}⚠ Circuit is currently HALF_OPEN${NC}"
        echo -e "${CYAN}Sending probe requests to close the circuit...${NC}"
        for i in {1..3}; do
            curl -s "${BASE_URL}/health/gateway" > /dev/null 2>&1
            sleep 0.5
        done
        sleep 1
        echo -e "${GREEN}✓ Circuit should now be CLOSED${NC}"
    fi
else
    echo -e "${GREEN}✓ Circuit is already CLOSED${NC}"
fi

echo ""
check_circuit_state
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 1: Initial requests (circuit should be CLOSED)
echo -e "${BLUE}=== Phase 1: Initial Requests (Circuit: CLOSED) ===${NC}"
echo -e "${CYAN}Sending initial requests to establish baseline...${NC}"
echo ""

for i in {1..3}; do
    make_health_request $i
    sleep 0.5
done

echo ""
check_circuit_state
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 2: Trigger failures to OPEN the circuit
echo -e "${BLUE}=== Phase 2: Triggering Circuit Breaker (CLOSED → OPEN) ===${NC}"
echo -e "${CYAN}Sending 10 requests to trigger failures...${NC}"
echo -e "${YELLOW}Need ≥2 failures in any 4-request sliding window (40% failure rate)${NC}"
echo ""

for i in {4..13}; do
    make_health_request $i
    sleep 0.3
done

echo ""
check_circuit_state
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 3: Circuit should be OPEN, requests should fail immediately
echo -e "${BLUE}=== Phase 3: Verify Circuit OPEN (Blocking Requests) ===${NC}"
echo -e "${CYAN}These requests should return circuit_open (503) instantly — no 500ms gateway delay${NC}"
echo ""

for i in {14..16}; do
    make_health_request $i
    sleep 0.5
done

echo ""
check_circuit_state
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 4: Wait for delay period to transition to HALF_OPEN
echo -e "${BLUE}=== Phase 4: Waiting for Recovery Period ===${NC}"
echo -e "${CYAN}Waiting 5 seconds for circuit breaker delay...${NC}"
echo -e "${YELLOW}Circuit should transition from OPEN → HALF_OPEN${NC}"
echo ""

for i in {5..1}; do
    echo -ne "${CYAN}Waiting... $i seconds remaining\r${NC}"
    sleep 1
done
echo ""

echo ""
check_circuit_state
echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Phase 5: Test HALF_OPEN state
echo -e "${BLUE}=== Phase 5: Testing HALF_OPEN State ===${NC}"
echo -e "${CYAN}Circuit should allow probe requests to test recovery...${NC}"
echo -e "${YELLOW}Need 2 consecutive successes to return to CLOSED${NC}"
echo ""

for i in {17..21}; do
    make_health_request $i
    sleep 1
    check_circuit_state
    echo ""
done

echo ""
echo -e "${BLUE}----------------------------------------${NC}"
echo ""

# Final state check
echo -e "${BLUE}=== Final Circuit Breaker State ===${NC}"
check_circuit_state
echo ""

# Show metrics
echo -e "${BLUE}=== Circuit Breaker Metrics ===${NC}"
echo -e "${CYAN}Fetching fault tolerance metrics...${NC}"
echo ""

# Use flexible pattern matching for metrics
all_metrics=$(curl -s "$METRICS_URL" 2>/dev/null)
metrics=$(echo "$all_metrics" | grep -i "circuitbreaker" | grep -i "checkGatewayHealth")

if [ -n "$metrics" ]; then
    echo "$metrics" | while IFS= read -r line; do
        if echo "$line" | grep -q "opened"; then
            echo -e "${YELLOW}$line${NC}"
        elif echo "$line" | grep -q "state"; then
            echo -e "${GREEN}$line${NC}"
        elif echo "$line" | grep -q "succeeded\|failed"; then
            echo -e "${CYAN}$line${NC}"
        else
            echo "$line"
        fi
    done
else
    # Try alternative metric patterns
    alt_metrics=$(echo "$all_metrics" | grep -E "ft.*Health.*circuit|circuit.*Health")
    if [ -n "$alt_metrics" ]; then
        echo -e "${CYAN}Found alternative circuit breaker metrics:${NC}"
        echo "$alt_metrics"
    else
        echo -e "${RED}✗ No circuit breaker metrics found${NC}"
        echo -e "${YELLOW}This could mean:${NC}"
        echo -e "${YELLOW}  • The service is not running${NC}"
        echo -e "${YELLOW}  • Metrics are not enabled${NC}"
        echo -e "${YELLOW}  • No health check requests were processed successfully${NC}"
    fi
fi

echo ""
echo -e "${GREEN}=== Circuit Breaker Test Complete ===${NC}"
echo ""
echo -e "${CYAN}To view all fault tolerance metrics:${NC}"
echo -e "${BLUE}curl $METRICS_URL | grep -i circuit${NC}"
echo ""
