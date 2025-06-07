/**
 * This package contains the Order Management application for the MicroProfile tutorial store.
 * 
 * The application demonstrates a Jakarta EE and MicroProfile-based REST service
 * for managing orders and order items with CRUD operations.
 * 
 * Main Components:
 * - Entity classes: Contains order data with order_id, user_id, total_price, status
 *   and order item data with order_item_id, order_id, product_id, quantity, price_at_order
 * - Repository: Provides in-memory data storage using HashMap
 * - Service: Contains business logic and validation
 * - Resource: REST endpoints with OpenAPI documentation
 */
package io.microprofile.tutorial.store.order;
