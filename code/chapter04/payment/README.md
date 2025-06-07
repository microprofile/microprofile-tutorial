# Payment Service

This microservice is part of the Jakarta EE and MicroProfile-based e-commerce application. It handles payment processing and transaction management.

## Features

- Payment transaction processing
- Multiple payment methods support
- Transaction status tracking
- Order payment integration
- User payment history

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

## Swagger UI

OpenAPI documentation is available at: `http://localhost:9050/payment/api/openapi-ui/`
