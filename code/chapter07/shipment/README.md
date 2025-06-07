# Shipment Service

This is the Shipment Service for the MicroProfile Tutorial e-commerce application. The service manages shipments for orders in the system.

## Overview

The Shipment Service is responsible for:
- Creating shipments for orders
- Tracking shipment status (PENDING, PROCESSING, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, RETURNED)
- Assigning tracking numbers
- Estimating delivery dates
- Communicating with the Order Service to update order status

## Technologies

The Shipment Service is built using:
- Jakarta EE 10
- MicroProfile 6.1
- Open Liberty
- Java 17

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.8+
- Docker (for containerized deployment)

### Running Locally

To build and run the service:

```bash
./run.sh
```

This will build the application and start the Open Liberty server. The service will be available at: http://localhost:8060/shipment

### Running with Docker

To build and run the service in a Docker container:

```bash
./run-docker.sh
```

This will build a Docker image for the service and run it, exposing ports 8060 and 9060.

## API Endpoints

| Method | URL                                        | Description                          |
|--------|-------------------------------------------|--------------------------------------|
| POST   | /api/shipments/orders/{orderId}           | Create a new shipment                |
| GET    | /api/shipments/{shipmentId}               | Get a shipment by ID                 |
| GET    | /api/shipments                            | Get all shipments                    |
| GET    | /api/shipments/status/{status}            | Get shipments by status              |
| GET    | /api/shipments/orders/{orderId}           | Get shipments for an order           |
| GET    | /api/shipments/tracking/{trackingNumber}  | Get a shipment by tracking number    |
| PUT    | /api/shipments/{shipmentId}/status/{status} | Update shipment status             |
| PUT    | /api/shipments/{shipmentId}/carrier       | Update shipment carrier              |
| PUT    | /api/shipments/{shipmentId}/tracking      | Update shipment tracking number      |
| PUT    | /api/shipments/{shipmentId}/delivery-date | Update estimated delivery date       |
| PUT    | /api/shipments/{shipmentId}/notes         | Update shipment notes                |
| DELETE | /api/shipments/{shipmentId}               | Delete a shipment                    |

## MicroProfile Features

The service utilizes several MicroProfile features:

- **Config**: For external configuration
- **Health**: For liveness and readiness checks
- **Metrics**: For monitoring service performance
- **Fault Tolerance**: For resilient communication with the Order Service
- **OpenAPI**: For API documentation

## Documentation

API documentation is available at:
- OpenAPI: http://localhost:8060/shipment/openapi
- Swagger UI: http://localhost:8060/shipment/openapi/ui

## Monitoring

Health and metrics endpoints:
- Health: http://localhost:8060/shipment/health
- Metrics: http://localhost:8060/shipment/metrics
