package io.microprofile.tutorial.store.order.resource;

import io.microprofile.tutorial.store.order.entity.OrderItem;
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
 * REST resource for order item operations.
 */
@Path("/orderItems")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OrderItem", description = "Operations related to order item management")
public class OrderItemResource {

    @Inject
    private OrderService orderService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/{id}")
    @Operation(summary = "Get order item by ID", description = "Returns a specific order item by ID")
    @APIResponse(
        responseCode = "200",
        description = "Order item",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = OrderItem.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Order item not found"
    )
    public OrderItem getOrderItemById(
        @Parameter(description = "ID of the order item", required = true)
        @PathParam("id") Long id) {
        return orderService.getOrderItemById(id);
    }

    @GET
    @Path("/order/{orderId}")
    @Operation(summary = "Get order items by order ID", description = "Returns items for a specific order")
    @APIResponse(
        responseCode = "200",
        description = "List of order items",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = OrderItem.class)
        )
    )
    public List<OrderItem> getOrderItemsByOrderId(
        @Parameter(description = "Order ID", required = true)
        @PathParam("orderId") Long orderId) {
        return orderService.getOrderItemsByOrderId(orderId);
    }

    @POST
    @Path("/order/{orderId}")
    @Operation(summary = "Add item to order", description = "Adds a new item to an existing order")
    @APIResponse(
        responseCode = "201",
        description = "Order item added",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = OrderItem.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found"
    )
    public Response addOrderItem(
        @Parameter(description = "ID of the order", required = true)
        @PathParam("orderId") Long orderId,
        @Parameter(description = "Order item details", required = true)
        @NotNull @Valid OrderItem orderItem) {
        OrderItem createdItem = orderService.addOrderItem(orderId, orderItem);
        URI location = uriInfo.getBaseUriBuilder()
                .path(OrderItemResource.class)
                .path(createdItem.getOrderItemId().toString())
                .build();
        return Response.created(location).entity(createdItem).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update order item", description = "Updates an existing order item")
    @APIResponse(
        responseCode = "200",
        description = "Order item updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = OrderItem.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Order item not found"
    )
    public OrderItem updateOrderItem(
        @Parameter(description = "ID of the order item", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "Updated order item details", required = true)
        @NotNull @Valid OrderItem orderItem) {
        return orderService.updateOrderItem(id, orderItem);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete order item", description = "Deletes an order item")
    @APIResponse(
        responseCode = "204",
        description = "Order item deleted"
    )
    @APIResponse(
        responseCode = "404",
        description = "Order item not found"
    )
    public Response deleteOrderItem(
        @Parameter(description = "ID of the order item", required = true)
        @PathParam("id") Long id) {
        orderService.deleteOrderItem(id);
        return Response.noContent().build();
    }
}
