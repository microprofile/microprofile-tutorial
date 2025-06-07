# Shopping Cart Service

This microservice is part of the Jakarta EE and MicroProfile-based e-commerce application. It handles shopping cart management for users.

## Features

- Create and manage user shopping carts
- Add products to cart with quantity
- Update and remove cart items
- Check product availability via the Inventory Service
- Fetch product details from the Catalog Service

## Endpoints

### GET /shoppingcart/api/carts
- Returns all shopping carts in the system

### GET /shoppingcart/api/carts/{id}
- Returns a specific shopping cart by ID

### GET /shoppingcart/api/carts/user/{userId}
- Returns or creates a shopping cart for a specific user

### POST /shoppingcart/api/carts/user/{userId}
- Creates a new shopping cart for a user

### POST /shoppingcart/api/carts/{cartId}/items
- Adds an item to a shopping cart
- Request body: CartItem JSON

### PUT /shoppingcart/api/carts/{cartId}/items/{itemId}
- Updates an item in a shopping cart
- Request body: Updated CartItem JSON

### DELETE /shoppingcart/api/carts/{cartId}/items/{itemId}
- Removes an item from a shopping cart

### DELETE /shoppingcart/api/carts/{cartId}/items
- Removes all items from a shopping cart

### DELETE /shoppingcart/api/carts/{cartId}
- Deletes a shopping cart

## Cart Item JSON Example

```json
{
  "productId": 1,
  "quantity": 2,
  "productName": "Product Name",   // Optional, will be fetched from Catalog if not provided
  "price": 29.99,                  // Optional, will be fetched from Catalog if not provided
  "imageUrl": "product-image.jpg"  // Optional, will be fetched from Catalog if not provided
}
```

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

The Shopping Cart Service integrates with:

- **Inventory Service**: Checks product availability before adding to cart
- **Catalog Service**: Retrieves product details (name, price, image)
- **Order Service**: Indirectly, when a cart is converted to an order

## MicroProfile Features Used

- **Config**: For service URL configuration
- **Fault Tolerance**: Circuit breakers, timeouts, retries, and fallbacks for resilient communication
- **Health**: Liveness and readiness checks
- **OpenAPI**: API documentation

## Swagger UI

OpenAPI documentation is available at: `http://localhost:4050/shoppingcart/api/openapi-ui/`
