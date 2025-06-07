package io.microprofile.tutorial.store.order.resource;

import io.microprofile.tutorial.store.order.entity.Order;
import io.microprofile.tutorial.store.order.entity.OrderStatus;
import io.microprofile.tutorial.store.order.service.OrderService;

import java.net.URI;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for order operations.
 */
@Path("/orders")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Order", description = "Operations related to order management")
public class OrderResource {

    @Inject
    private OrderService orderService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Get all orders", description = "Returns a list of all orders with their items")
    @APIResponse(
        responseCode = "200",
        description = "List of orders",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Order.class)
        )
    )
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get order by ID", description = "Returns a specific order by ID with its items")
    @APIResponse(
        responseCode = "200",
        description = "Order",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Order.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found"
    )
    public Order getOrderById(
        @Parameter(description = "ID of the order", required = true)
        @PathParam("id") Long id) {
        return orderService.getOrderById(id);
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get orders by user ID", description = "Returns orders for a specific user")
    @APIResponse(
        responseCode = "200",
        description = "List of orders",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Order.class)
        )
    )
    public List<Order> getOrdersByUserId(
        @Parameter(description = "User ID", required = true)
        @PathParam("userId") Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Returns orders with a specific status")
    @APIResponse(
        responseCode = "200",
        description = "List of orders",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Order.class)
        )
    )
    public List<Order> getOrdersByStatus(
        @Parameter(description = "Order status", required = true)
        @PathParam("status") String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderService.getOrdersByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid order status: " + status, Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Operation(summary = "Create new order", description = "Creates a new order with items")
    @APIResponse(
        responseCode = "201",
        description = "Order created",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Order.class)
        )
    )
    public Response createOrder(
        @Parameter(description = "Order details", required = true)
        @NotNull @Valid Order order) {
        Order createdOrder = orderService.createOrder(order);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdOrder.getOrderId().toString()).build();
        return Response.created(location).entity(createdOrder).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update order", description = "Updates an existing order")
    @APIResponse(
        responseCode = "200",
        description = "Order updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Order.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found"
    )
    public Order updateOrder(
        @Parameter(description = "ID of the order", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "Updated order details", required = true)
        @NotNull @Valid Order order) {
        return orderService.updateOrder(id, order);
    }

    @PATCH
    @Path("/{id}/status/{status}")
    @Operation(summary = "Update order status", description = "Updates the status of an order")
    @APIResponse(
        responseCode = "200",
        description = "Order status updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Order.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid order status"
    )
    public Order updateOrderStatus(
        @Parameter(description = "ID of the order", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "New order status", required = true)
        @PathParam("status") String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderService.updateOrderStatus(id, orderStatus);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid order status: " + status, Response.Status.BAD_REQUEST);
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete order", description = "Deletes an order and its items")
    @APIResponse(
        responseCode = "204",
        description = "Order deleted"
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found"
    )
    public Response deleteOrder(
        @Parameter(description = "ID of the order", required = true)
        @PathParam("id") Long id) {
        orderService.deleteOrder(id);
        return Response.noContent().build();
    }
}
