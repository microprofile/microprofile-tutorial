# Inventory Service REST API Test Scripts

This directory contains comprehensive test scripts for the Inventory Service REST API, including full MicroProfile Rest Client integration testing.

## Test Scripts

### 1. `test-inventory-endpoints.sh` - Complete Test Suite

A comprehensive test script that covers all inventory endpoints and MicroProfile Rest Client features.

#### Usage:
```bash
# Run all tests
./test-inventory-endpoints.sh

# Run specific test suites
./test-inventory-endpoints.sh --basic      # Basic CRUD operations only
./test-inventory-endpoints.sh --restclient # RestClient functionality only  
./test-inventory-endpoints.sh --performance # Performance tests only
./test-inventory-endpoints.sh --help       # Show help
```

#### Test Coverage:
- ✅ **Basic CRUD Operations**: Create, Read, Update, Delete inventory
- ✅ **MicroProfile Rest Client Integration**: Product validation using `@RestClient` injection
- ✅ **RestClientBuilder Functionality**: Programmatic client creation with custom timeouts
- ✅ **Error Handling**: Non-existent products, conflicts, validation errors
- ✅ **Pagination & Filtering**: Query parameters, count operations
- ✅ **Bulk Operations**: Batch create/delete
- ✅ **Advanced Features**: Inventory reservation, product enrichment
- ✅ **Performance Testing**: Response time comparison between different client approaches

### 2. `quick-test-commands.sh` - Command Reference

A quick reference showing all available curl commands for manual testing.

#### Usage:
```bash
./quick-test-commands.sh  # Display all available commands
```

## MicroProfile Rest Client Features Tested

### 1. Injected REST Client (`@RestClient`)
Used for product validation in create/update operations:
```java
@Inject
@RestClient
private ProductServiceClient productServiceClient;
```

**Endpoints that use this:**
- `POST /inventories` - Create inventory
- `PUT /inventories/{id}` - Update inventory
- `POST /inventories/bulk` - Bulk create

### 2. RestClientBuilder (5s/10s timeout)
Used for lightweight product availability checks:
```java
ProductServiceClient dynamicClient = RestClientBuilder.newBuilder()
    .baseUri(catalogServiceUri)
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build(ProductServiceClient.class);
```

**Endpoints that use this:**
- `PATCH /inventories/product/{productId}/reserve/{quantity}` - Reserve inventory

### 3. Advanced RestClientBuilder (3s/8s timeout)
Used for detailed product information retrieval:
```java
ProductServiceClient customClient = RestClientBuilder.newBuilder()
    .baseUri(catalogServiceUri)
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(8, TimeUnit.SECONDS)
    .build(ProductServiceClient.class);
```

**Endpoints that use this:**
- `GET /inventories/product-info/{productId}` - Get product details

## Prerequisites

### Required Services:
- **Catalog Service**: Running on `http://localhost:5050`
- **Inventory Service**: Running on `http://localhost:7050`

### Required Tools:
- `curl` - For HTTP requests
- `jq` - For JSON formatting (install with `sudo apt-get install jq`)

## Example Test Run

```bash
# Make script executable
chmod +x test-inventory-endpoints.sh

# Run RestClient tests only
./test-inventory-endpoints.sh --restclient
```

### Expected Output:
```
============================================================================
🔌 RESTCLIENTBUILDER - PRODUCT AVAILABILITY
============================================================================

TEST: Reserve 10 units of product 1 (uses RestClientBuilder)
COMMAND: curl -X PATCH 'http://localhost:7050/inventory/api/inventories/product/1/reserve/10'
{
  "inventoryId": 1,
  "productId": 1,
  "quantity": 100,
  "reservedQuantity": 10
}
```

## API Endpoints Summary

| Method | Endpoint | Description | RestClient Type |
|--------|----------|-------------|-----------------|
| GET | `/inventories` | Get all inventories | None |
| POST | `/inventories` | Create inventory | @RestClient (validation) |
| GET | `/inventories/{id}` | Get inventory by ID | None |
| PUT | `/inventories/{id}` | Update inventory | @RestClient (validation) |
| DELETE | `/inventories/{id}` | Delete inventory | None |
| GET | `/inventories/product/{productId}` | Get inventory by product ID | None |
| PATCH | `/inventories/product/{productId}/reserve/{quantity}` | Reserve inventory | RestClientBuilder (5s/10s) |
| GET | `/inventories/product-info/{productId}` | Get product info | RestClientBuilder (3s/8s) |
| GET | `/inventories/{id}/with-product-info` | Get enriched inventory | @RestClient (enrichment) |
| POST | `/inventories/bulk` | Bulk create inventories | @RestClient (validation) |

## Configuration

The inventory service connects to the catalog service using these configurations:

**MicroProfile Config** (`microprofile-config.properties`):
```properties
io.microprofile.tutorial.store.inventory.client.ProductServiceClient/mp-rest/url=http://localhost:5050/catalog/api
io.microprofile.tutorial.store.inventory.client.ProductServiceClient/mp-rest/scope=javax.inject.Singleton
```

**RestClientBuilder** (programmatic):
```java
URI catalogServiceUri = URI.create("http://localhost:5050/catalog/api");
```

## Troubleshooting

### Service Not Available
```bash
❌ Catalog Service is not available
❌ Inventory Service is not available
```
**Solution**: Start both services:
```bash
# Terminal 1 - Catalog Service
cd /workspaces/liberty-rest-app/catalog && mvn liberty:run

# Terminal 2 - Inventory Service  
cd /workspaces/liberty-rest-app/inventory && mvn liberty:dev
```

### jq Not Found
```bash
❌ jq is required for JSON formatting
```
**Solution**: Install jq:
```bash
sudo apt-get install jq  # Ubuntu/Debian
brew install jq          # macOS
```

### Inventory Not Found Errors
If you see "Inventory not found" errors, create some test inventory first:
```bash
curl -X POST 'http://localhost:7050/inventory/api/inventories' \
  -H 'Content-Type: application/json' \
  -d '{"productId": 1, "quantity": 100, "reservedQuantity": 0}'
```

## Success Criteria

A successful test run should show:
- ✅ Both services responding
- ✅ Product validation working (catalog service integration)
- ✅ RestClientBuilder creating clients with custom timeouts
- ✅ Proper error handling for non-existent products
- ✅ All CRUD operations functioning
- ✅ Reservation system working with availability checks
