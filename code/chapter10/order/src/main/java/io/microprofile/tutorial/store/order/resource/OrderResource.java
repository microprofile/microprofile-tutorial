package io.microprofile.tutorial.store.order.resource;

import io.microprofile.tutorial.store.order.entity.Order;
import io.microprofile.tutorial.store.order.entity.Order.OrderStatus;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * REST resource for managing orders in the e-commerce system.
 * Provides CRUD operations and order management functionality.
 */
@Path("/orders")
@Tag(name = "Order Management", description = "CRUD operations and order management for the e-commerce store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT authentication with bearer token"
)
public class OrderResource {

    private static final Logger LOGGER = Logger.getLogger(OrderResource.class.getName());

    // AtomicLong for generating unique order IDs
    private static final java.util.concurrent.atomic.AtomicLong idGenerator = new java.util.concurrent.atomic.AtomicLong(1);

    // In-memory store for orders
    private final java.util.Map<Long, Order> orders = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Get order by ID - Accessible only to users with the "user" role
     */
    @RolesAllowed("user") // Only users can access this method
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @SecurityRequirement(name = "jwt")
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Order found", 
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = Order.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token is missing or invalid"
        ),
        @APIResponse(responseCode = "404", description = "Order not found")
    })
    @GET
    @Path("/{id}")
    public Response getOrder(
            @PathParam("id") 
            @Parameter(description = "Order ID") 
            Long id,
            @Context SecurityContext ctx) {
        
        String user = ctx.getUserPrincipal().getName();
        LOGGER.info("User " + user + " fetching order with ID: " + id);
        
        // Fetch order from the map
        Order order = orders.get(id);
        if (order == null) {
            LOGGER.warning("Order not found with ID: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Order not found with ID: " + id + "\"}")
                    .build();
        }
        
        // For a real application, verify that the user is allowed to access this order
        // (e.g., it's their own order or they have appropriate permissions)
        
        return Response.ok(order).build();
    }

    /**
     * Delete an order - Accessible only to users with the "admin" role
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin") // Only admins can access this method
    @Operation(summary = "Delete an order", description = "Delete an order by its ID")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Order deleted successfully"),
        @APIResponse(responseCode = "404", description = "Order not found")
    })
    @SecurityRequirement(name = "jwt")
    public Response deleteOrder(
            @PathParam("id") 
            @Parameter(description = "Order ID") 
            Long id,
            @Context SecurityContext ctx) {
        
        String admin = ctx.getUserPrincipal().getName();
        LOGGER.info("Admin " + admin + " deleting order with ID: " + id);
        
        // Try to remove the order from the map
        Order removedOrder = orders.remove(id);
        if (removedOrder != null) {
            LOGGER.info("Order deleted successfully with ID: " + id + " by admin: " + admin);
            return Response.noContent().build();
        } else {
            LOGGER.warning("Order not found with ID: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Order not found with ID: " + id + "\"}")
                    .build();
        }
    }
    
    /**
     * Create a new order
     */
    @POST
    @Operation(summary = "Create a new order", description = "Create a new order in the system")
    @APIResponses({
        @APIResponse(
            responseCode = "201", 
            description = "Order created successfully", 
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = Order.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Invalid order data")
    })
    public Response createOrder(@Valid @NotNull Order order) {
        LOGGER.info("Creating new order for customer: " + order.getCustomerId());
        
        // Set generated values
        Long id = idGenerator.getAndIncrement();
        order.setId(id);
        order.setOrderDate(LocalDateTime.now());
        order.setLastModified(LocalDateTime.now());
        
        // Set default status if not provided
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        
        orders.put(id, order);
        
        LOGGER.info("Order created successfully with ID: " + id);
        return Response.created(UriBuilder.fromResource(OrderResource.class)
                .path(String.valueOf(id)).build())
                .entity(order)
                .build();
    }
    
    /**
     * Update an existing order
     */
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an order", description = "Update an existing order by its ID")
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Order updated successfully", 
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = Order.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "Order not found"),
        @APIResponse(responseCode = "400", description = "Invalid order data")
    })
    public Response updateOrder(
            @PathParam("id") 
            @Parameter(description = "Order ID") 
            Long id, 
            @Valid @NotNull Order updatedOrder) {
        
        LOGGER.info("Updating order with ID: " + id);
        
        Order existingOrder = orders.get(id);
        if (existingOrder == null) {
            LOGGER.warning("Order not found with ID: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Order not found with ID: " + id + "\"}")
                    .build();
        }
        
        // Preserve ID and creation date
        updatedOrder.setId(id);
        updatedOrder.setOrderDate(existingOrder.getOrderDate());
        updatedOrder.setLastModified(LocalDateTime.now());
        
        // Calculate and validate total amounts
        validateAndCalculateTotals(updatedOrder);
        
        orders.put(id, updatedOrder);
        
        LOGGER.info("Order updated successfully with ID: " + id);
        return Response.ok(updatedOrder).build();
    }
    
    /**
     * Update order status
     */
    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Order status updated successfully", 
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = Order.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "Order not found"),
        @APIResponse(responseCode = "400", description = "Invalid status")
    })
    public Response updateOrderStatus(
            @PathParam("id") 
            @Parameter(description = "Order ID") 
            Long id,
            
            @QueryParam("status") 
            @Parameter(description = "New order status", required = true) 
            @NotNull OrderStatus newStatus) {
        
        LOGGER.info("Updating order status for ID: " + id + " to: " + newStatus);
        
        Order order = orders.get(id);
        if (order == null) {
            LOGGER.warning("Order not found with ID: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Order not found with ID: " + id + "\"}")
                    .build();
        }
        
        LOGGER.info("Order status updated successfully for ID: " + id);
        return Response.ok(order).build();
    }
    
    /**
     * This method was removed as it was a duplicate of the admin-only DELETE method above
     */
    
    /**
     * Get orders by customer ID
     */
    @GET
    @Path("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID", description = "Retrieve all orders for a specific customer")
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Customer orders retrieved successfully", 
            content = @Content(
                schema = @Schema(implementation = Order.class, type = SchemaType.ARRAY)
            )
        )
    })
    public Response getOrdersByCustomerId(
            @PathParam("customerId") 
            @Parameter(description = "Customer ID") 
            String customerId) {
        
        LOGGER.info("Fetching orders for customer: " + customerId);
        
        List<Order> customerOrders = orders.values().stream()
                .filter(order -> customerId.equals(order.getCustomerId()))
                .collect(Collectors.toList());
        
        return Response.ok(customerOrders).build();
    }
    
    /**
     * Get order statistics
     */
    @GET
    @Path("/stats")
    @Operation(summary = "Get order statistics", description = "Get statistical information about orders")
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Order statistics retrieved successfully", 
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    public Response getOrderStatistics() {
        LOGGER.info("Fetching order statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orders.size());
        
        Map<OrderStatus, Long> statusCounts = orders.values().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        stats.put("ordersByStatus", statusCounts);
        
        OptionalDouble avgTotal = orders.values().stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .average();
        stats.put("averageOrderValue", avgTotal.orElse(0.0));
        
        Optional<BigDecimal> maxTotal = orders.values().stream()
                .map(Order::getTotalAmount)
                .max(java.util.Comparator.naturalOrder());
        stats.put("maxOrderValue", maxTotal.orElse(BigDecimal.ZERO));
        
        return Response.ok(stats).build();
    }
    
    /**
     * Validate and calculate order totals
     */
    private void validateAndCalculateTotals(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
        
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (Order.OrderItem item : order.getItems()) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setTotalPrice(itemTotal);
            calculatedTotal = calculatedTotal.add(itemTotal);
        }
        
        order.setTotalAmount(calculatedTotal);
    }
}
