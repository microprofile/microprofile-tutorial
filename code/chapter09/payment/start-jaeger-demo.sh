#!/bin/bash

echo "=== Jaeger Telemetry Demo for Payment Service ==="
echo "This script starts Jaeger and demonstrates distributed tracing"
echo

# Function to check if a service is running
check_service() {
    local url=$1
    local service_name=$2
    echo "Checking if $service_name is accessible..."
    
    for i in {1..30}; do
        if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|404"; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        echo "⏳ Waiting for $service_name... ($i/30)"
        sleep 2
    done
    
    echo "❌ $service_name is not responding"
    return 1
}

# Start Jaeger using Docker Compose
echo "🚀 Starting Jaeger..."
docker-compose -f docker-compose-jaeger.yml up -d

# Wait for Jaeger to be ready
if check_service "http://localhost:16686" "Jaeger UI"; then
    echo
    echo "🎯 Jaeger is now running!"
    echo "📊 Jaeger UI: http://localhost:16686"
    echo "🔧 Collector endpoint: http://localhost:14268/api/traces"
    echo
    
    # Check if Liberty server is running
    if check_service "http://localhost:9080/payment/health" "Payment Service"; then
        echo
        echo "🧪 Testing Payment Service with Telemetry..."
        echo
        
        # Test 1: Basic payment processing
        echo "📝 Test 1: Processing a successful payment..."
        curl -X POST http://localhost:9080/payment/payments \
             -H "Content-Type: application/json" \
             -d '{
                 "cardNumber": "4111111111111111",
                 "expiryDate": "12/25",
                 "cvv": "123",
                 "amount": 99.99,
                 "currency": "USD",
                 "merchantId": "MERCHANT_001"
             }' \
             -w "\nResponse Code: %{http_code}\n\n"
        
        sleep 2
        
        # Test 2: Payment with fraud check failure
        echo "📝 Test 2: Processing payment that will trigger fraud check..."
        curl -X POST http://localhost:9080/payment/payments \
             -H "Content-Type: application/json" \
             -d '{
                 "cardNumber": "4111111111110000",
                 "expiryDate": "12/25",
                 "cvv": "123",
                 "amount": 50.00,
                 "currency": "USD",
                 "merchantId": "MERCHANT_002"
             }' \
             -w "\nResponse Code: %{http_code}\n\n"
        
        sleep 2
        
        # Test 3: Payment with insufficient funds
        echo "📝 Test 3: Processing payment with insufficient funds..."
        curl -X POST http://localhost:9080/payment/payments \
             -H "Content-Type: application/json" \
             -d '{
                 "cardNumber": "4111111111111111",
                 "expiryDate": "12/25",
                 "cvv": "123",
                 "amount": 1500.00,
                 "currency": "USD",
                 "merchantId": "MERCHANT_003"
             }' \
             -w "\nResponse Code: %{http_code}\n\n"
        
        sleep 2
        
        # Test 4: Multiple concurrent payments to demonstrate distributed tracing
        echo "📝 Test 4: Generating multiple concurrent payments..."
        for i in {1..5}; do
            curl -X POST http://localhost:9080/payment/payments \
                 -H "Content-Type: application/json" \
                 -d "{
                     \"cardNumber\": \"41111111111111$i$i\",
                     \"expiryDate\": \"12/25\",
                     \"cvv\": \"123\",
                     \"amount\": $((10 + i * 10)).99,
                     \"currency\": \"USD\",
                     \"merchantId\": \"MERCHANT_00$i\"
                 }" \
                 -w "\nBatch $i Response Code: %{http_code}\n" &
        done
        
        # Wait for all background requests to complete
        wait
        
        echo
        echo "🎉 All tests completed!"
        echo
        echo "🔍 View Traces in Jaeger:"
        echo "   1. Open http://localhost:16686 in your browser"
        echo "   2. Select 'payment-service' from the Service dropdown"
        echo "   3. Click 'Find Traces' to see all the traces"
        echo
        echo "📈 You should see traces for:"
        echo "   • Payment processing operations"
        echo "   • Fraud check steps"
        echo "   • Funds verification"
        echo "   • Transaction recording"
        echo "   • Fault tolerance retries (if any failures occurred)"
        echo
        
    else
        echo "❌ Payment service is not running. Please start it with:"
        echo "   mvn liberty:dev"
    fi
    
else
    echo "❌ Failed to start Jaeger. Please check Docker and try again."
    exit 1
fi

echo "🛑 To stop Jaeger when done:"
echo "   docker-compose -f docker-compose-jaeger.yml down"
