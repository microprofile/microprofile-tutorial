

## Endpoints

### GET /payment/api/payments
- Returns all payments in the system

### GET /payment/api/payments/{id}
- Returns a specific payment by ID

### GET /payment/api/payments/user/{userId}
- Returns all payments for a specific user

### GET /payment/api/payments/order/{orderId}
- Returns all payments for a specific order

### GET /payment/api/payments/status/{status}
- Returns all payments with a specific status

### POST /payment/api/payments
- Creates a new payment
- Request body: Payment JSON

### PUT /payment/api/payments/{id}
- Updates an existing payment
- Request body: Updated Payment JSON

### PATCH /payment/api/payments/{id}/status/{status}
- Updates the status of an existing payment

### POST /payment/api/payments/{id}/process
- Processes a pending payment

### DELETE /payment/api/payments/{id}
- Deletes a payment

## Payment Flow

1. Create a payment with status `PENDING`
2. Process the payment to change status to `PROCESSING`
3. Payment will automatically be updated to either `COMPLETED` or `FAILED`
4. If needed, payments can be `REFUNDED` or `CANCELLED`

## Running the Service

### Local Development

```bash
./run.sh
```

### Docker

```bash
./run-docker.sh
```

## Integration with Other Services

The Payment Service integrates with:

- **Order Service**: Updates order status based on payment status
- **User Service**: Validates user information for payment processing

## Testing

For testing purposes, payments with amounts ending in `.00` will fail, all others will succeed.

## Custom ConfigSource

The Payment Service implements a custom MicroProfile ConfigSource named `PaymentServiceConfigSource` that provides payment-specific configuration with high priority (ordinal: 500).

### Available Configuration Properties

| Property | Description | Default Value |
|----------|-------------|---------------|
| payment.gateway.endpoint | Payment gateway endpoint URL | https://secure-payment-gateway.example.com/api/v1 |

### ConfigSource Endpoints

The custom ConfigSource can be accessed and modified via the following endpoints:

#### GET /payment/api/payment-config
- Returns all current payment configuration values

#### POST /payment/api/payment-config
- Updates a payment configuration value
- Request body: `{"key": "payment.property.name", "value": "new-value"}`

### Example Usage

```java
// Inject standard MicroProfile Config
@Inject
@ConfigProperty(name="payment.gateway.endpoint")
String gatewayUrl;

// Or use the utility class
String url = PaymentConfig.getConfigProperty("payment.gateway.endpoint");
```

The custom ConfigSource provides a higher priority than system properties and environment variables, allowing for service-specific defaults while still enabling override via standard mechanisms.

## Swagger UI

OpenAPI documentation is available at: `http://localhost:9050/payment/api/openapi-ui/`
