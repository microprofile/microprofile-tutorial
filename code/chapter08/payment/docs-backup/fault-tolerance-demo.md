# MicroProfile Fault Tolerance Demo - Payment Service

## Implementation Summary

The Payment Service has been successfully enhanced with comprehensive MicroProfile Fault Tolerance patterns. Here's what has been implemented:

### ✅ Completed Features

#### 1. Server Configuration
- Added `mpFaultTolerance` feature to `server.xml`
- Configured Liberty server with MicroProfile 6.1 platform

#### 2. PaymentService Class Enhancements
- **Scope Change**: Modified from `@RequestScoped` to `@ApplicationScoped` for proper fault tolerance behavior
- **Fault Tolerance Annotations**: Applied comprehensive retry, circuit breaker, timeout, and fallback patterns

#### 3. Implemented Retry Policies

##### Authorization Retry (@Retry)
```java
@Retry(
        maxRetries = 3,
        delay = 2000,
        jitter = 500,
        retryOn = PaymentProcessingException.class,
        abortOn = CriticalPaymentException.class
    )
```
- **Use Case**: Standard payment authorization with exponential backoff
- **Trigger**: Card numbers ending in "0000" simulate failures
- **Fallback**: Returns service unavailable response

##### Verification Retry (Aggressive)
```java
@Retry(
        maxRetries = 3,
        delay = 2000,
        jitter = 500,
        retryOn = PaymentProcessingException.class,
        abortOn = CriticalPaymentException.class
    )
```
- **Use Case**: Critical verification operations that must succeed
- **Trigger**: Random 50% failure rate for demonstration
- **Fallback**: Returns verification unavailable response

##### Capture with Circuit Breaker
```java
@Retry(
    maxRetries = 2,
    delay = 2000,
    delayUnit = ChronoUnit.MILLIS,
    retryOn = {RuntimeException.class}
)
@CircuitBreaker(
    failureRatio = 0.5,
    requestVolumeThreshold = 4,
    delay = 5000,
    delayUnit = ChronoUnit.MILLIS
)
@Timeout(value = 3000, unit = ChronoUnit.MILLIS)
@Fallback(fallbackMethod = "fallbackPaymentCapture")
```
- **Use Case**: External service calls with protection against cascading failures
- **Trigger**: Random 30% failure rate with 1-4 second delays
- **Circuit Breaker**: Opens after 50% failure rate over 4 requests
- **Fallback**: Queues capture for retry

##### Refund Retry (Conservative)
```java
@Retry(
    maxRetries = 1,
    delay = 3000,
    delayUnit = ChronoUnit.MILLIS,
    retryOn = {RuntimeException.class},
    abortOn = {IllegalArgumentException.class}
)
@Fallback(fallbackMethod = "fallbackPaymentRefund")
```
- **Use Case**: Financial operations requiring careful handling
- **Trigger**: 40% random failure rate, empty amount triggers abort
- **Abort Condition**: Invalid input immediately fails without retry
- **Fallback**: Queues for manual processing

#### 4. Configuration Management
Enhanced `PaymentServiceConfigSource` with fault tolerance properties:
- `payment.retry.maxRetries=3`
- `payment.retry.delay=1000`
- `payment.circuitbreaker.failureRatio=0.5`
- `payment.circuitbreaker.requestVolumeThreshold=4`
- `payment.timeout.duration=3000`

#### 5. API Endpoints with Fault Tolerance
- `/api/authorize` - Authorization with retry (3 attempts)
- `/api/verify` - Verification with aggressive retry (5 attempts)
- `/api/capture` - Capture with circuit breaker + timeout protection
- `/api/refund` - Conservative retry with abort conditions

#### 6. Fallback Mechanisms
All operations provide graceful degradation:
- **Authorization**: Service unavailable response
- **Verification**: Verification unavailable, queue for retry
- **Capture**: Defer operation response
- **Refund**: Manual processing queue response

#### 7. Documentation Updates
- **README.adoc**: Comprehensive fault tolerance documentation
- **index.html**: Updated web interface with fault tolerance features
- **Test Script**: Complete testing scenarios (`test-fault-tolerance.sh`)

### 🎯 Testing Scenarios

#### Manual Testing Examples

1. **Test Authorization Retry**:
```bash
curl -X POST http://<hostname>host:9080/payment/api/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111110000",
    "cardHolderName": "Test User",
    "expiryDate": "12/25",
    "securityCode": "123",
    "amount": 100.00
  }'
```
- Card ending in "0000" triggers retries and fallback

2. **Test Verification with Random Failures**:
```bash
curl -X POST http://<hostname>:9080/payment/api/verify?transactionId=TXN1234567890
```
- 50% chance of failure triggers aggressive retry policy

3. **Test Circuit Breaker**:
```bash
for i in {1..10}; do
  curl -X POST http://<hostname>:9080/payment/api/capture?transactionId=TXN$i
  echo ""
  sleep 1
done
```
- Multiple failures will open the circuit breaker

4. **Test Conservative Refund**:
```bash
# Valid refund
curl -X POST http://<hostname>:9080/payment/api/refund?transactionId=TXN123&amount=50.00

# Invalid refund (triggers abort)
curl -X POST http://<hostname>:9080/payment/api/refund?transactionId=TXN123&amount=
```

### 📊 Monitoring and Observability

#### Log Monitoring
```bash
tail -f target/liberty/wlp/usr/servers/mpServer/logs/messages.log
```

#### Metrics (when available)
```bash
# Fault tolerance metrics
curl http://<hostname>:9080/payment/metrics/application

# Specific retry metrics
curl http://<hostname>:9080/payment/metrics/application?name=ft.retry.calls.total

# Circuit breaker metrics
curl http://<hostname>:9080/payment/metrics/application?name=ft.circuitbreaker.calls.total
```

### 🔧 Configuration Properties

| Property | Description | Default Value |
|----------|-------------|---------------|
| `payment.gateway.endpoint` | Payment gateway endpoint URL | `https://api.paymentgateway.com` |
| `payment.retry.maxRetries` | Maximum retry attempts | `3` |
| `payment.retry.delay` | Delay between retries (ms) | `1000` |
| `payment.circuitbreaker.failureRatio` | Circuit breaker failure ratio | `0.5` |
| `payment.circuitbreaker.requestVolumeThreshold` | Min requests for evaluation | `4` |
| `payment.timeout.duration` | Timeout duration (ms) | `3000` |

### 🎉 Benefits Achieved

1. **Resilience**: Services gracefully handle transient failures
2. **Stability**: Circuit breakers prevent cascading failures
3. **User Experience**: Fallback mechanisms provide immediate responses
4. **Observability**: Comprehensive logging and metrics support
5. **Configurability**: Dynamic configuration through MicroProfile Config
6. **Enterprise-Ready**: Production-grade fault tolerance patterns

## Running the Complete Demo

1. **Build and Start**:
```bash
cd /workspaces/liberty-rest-app/payment
mvn clean package
mvn liberty:run
```

2. **Run Test Suite**:
```bash
chmod +x test-fault-tolerance.sh
./test-fault-tolerance.sh
```

3. **Monitor Behavior**:
```bash
tail -f target/liberty/wlp/usr/servers/mpServer/logs/messages.log
```

The Payment Service now demonstrates enterprise-grade fault tolerance with MicroProfile patterns, making it resilient to failures and suitable for production microservices environments.
