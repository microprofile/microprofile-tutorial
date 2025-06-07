# Order Service

A Jakarta EE and MicroProfile-based REST service for order management in the Liberty Rest App demo.

## Features

- Provides CRUD operations for order management
- Tracks orders with order_id, user_id, total_price, and status
- Manages order items with order_item_id, order_id, product_id, quantity, and price_at_order
- Uses Jakarta EE 10.0 and MicroProfile 6.1
- Runs on Open Liberty runtime

## Running the Application

There are multiple ways to run the application:

### Using Maven

```
cd order
mvn liberty:run
```

### Using the provided script

```
./run.sh
```

### Using Docker

```
./run-docker.sh
```

This will start the Open Liberty server on port 8050 (HTTP) and 8051 (HTTPS).

## API Endpoints

| Method | URL                                     | Description                          |
|--------|:----------------------------------------|:-------------------------------------|
| GET    | /api/orders                             | Get all orders                       |
| GET    | /api/orders/{id}                        | Get order by ID                      |
| GET    | /api/orders/user/{userId}               | Get orders by user ID                |
| GET    | /api/orders/status/{status}             | Get orders by status                 |
| POST   | /api/orders                             | Create new order                     |
| PUT    | /api/orders/{id}                        | Update order                         |
| DELETE | /api/orders/{id}                        | Delete order                         |
| PATCH  | /api/orders/{id}/status/{status}        | Update order status                  |
| GET    | /api/orders/{orderId}/items             | Get items for an order               |
| GET    | /api/orders/items/{orderItemId}         | Get specific order item              |
| POST   | /api/orders/{orderId}/items             | Add item to order                    |
| PUT    | /api/orders/items/{orderItemId}         | Update order item                    |
| DELETE | /api/orders/items/{orderItemId}         | Delete order item                    |

## Testing with cURL

### Get all orders
```
curl -X GET http://localhost:8050/order/api/orders
```

### Get order by ID
```
curl -X GET http://localhost:8050/order/api/orders/1
```

### Get orders by user ID
```
curl -X GET http://localhost:8050/order/api/orders/user/1
```

### Create new order
```
curl -X POST http://localhost:8050/order/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "totalPrice": 149.98,
    "status": "CREATED",
    "orderItems": [
      {
        "productId": 101,
        "quantity": 2,
        "priceAtOrder": 49.99
      },
      {
        "productId": 102,
        "quantity": 1,
        "priceAtOrder": 50.00
      }
    ]
  }'
```

### Update order
```
curl -X PUT http://localhost:8050/order/api/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "totalPrice": 149.98,
    "status": "PAID"
  }'
```

### Update order status
```
curl -X PATCH http://localhost:8050/order/api/orders/1/status/SHIPPED
```

### Delete order
```
curl -X DELETE http://localhost:8050/order/api/orders/1
```

### Get items for an order
```
curl -X GET http://localhost:8050/order/api/orders/1/items
```

### Add item to order
```
curl -X POST http://localhost:8050/order/api/orders/1/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 103,
    "quantity": 1,
    "priceAtOrder": 29.99
  }'
```

### Update order item
```
curl -X PUT http://localhost:8050/order/api/orders/items/1 \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "productId": 103,
    "quantity": 2,
    "priceAtOrder": 29.99
  }'
```

### Delete order item
```
curl -X DELETE http://localhost:8050/order/api/orders/items/1
```
