package io.microprofile.tutorial.store.inventory.resource;

import io.microprofile.tutorial.store.inventory.entity.Inventory;
import io.microprofile.tutorial.store.inventory.service.InventoryService;
import io.microprofile.tutorial.store.inventory.dto.Product;

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
 * REST resource for inventory operations.
 */
@Path("/inventories")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory", description = "Operations related to product inventory management")
public class InventoryResource {

    @Inject
    private InventoryService inventoryService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Get all inventory items", description = "Returns a paginated list of inventory items with optional filtering")
    @APIResponse(
        responseCode = "200",
        description = "List of inventory items",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Inventory.class)
        )
    )
    public Response getAllInventories(
        @Parameter(description = "Page number (zero-based)", schema = @Schema(defaultValue = "0"))
        @QueryParam("page") @DefaultValue("0") int page,
        
        @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
        @QueryParam("size") @DefaultValue("20") int size,
        
        @Parameter(description = "Filter by minimum quantity")
        @QueryParam("minQuantity") Integer minQuantity,
        
        @Parameter(description = "Filter by maximum quantity")
        @QueryParam("maxQuantity") Integer maxQuantity) {
        
        List<Inventory> inventories = inventoryService.getAllInventories(page, size, minQuantity, maxQuantity);
        long totalCount = inventoryService.countInventories(minQuantity, maxQuantity);
        
        return Response.ok(inventories)
                .header("X-Total-Count", totalCount)
                .header("X-Page-Number", page)
                .header("X-Page-Size", size)
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get inventory item by ID", description = "Returns a specific inventory item by ID")
    @APIResponse(
        responseCode = "200",
        description = "Inventory item",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Inventory.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Inventory not found"
    )
    public Inventory getInventoryById(
        @Parameter(description = "ID of the inventory item", required = true)
        @PathParam("id") Long id) {
        return inventoryService.getInventoryById(id);
    }

    @GET
    @Path("/product/{productId}")
    @Operation(summary = "Get inventory item by product ID", description = "Returns inventory information for a specific product")
    @APIResponse(
        responseCode = "200",
        description = "Inventory item",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Inventory.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Inventory not found for product"
    )
    public Inventory getInventoryByProductId(
        @Parameter(description = "Product ID", required = true)
        @PathParam("productId") Long productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @POST
    @Operation(summary = "Create new inventory item", description = "Creates a new inventory item")
    @APIResponse(
        responseCode = "201",
        description = "Inventory created",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Inventory.class)
        )
    )
    @APIResponse(
        responseCode = "409",
        description = "Inventory for product already exists"
    )
    public Response createInventory(
        @Parameter(description = "Inventory details", required = true)
        @NotNull @Valid Inventory inventory) {
        Inventory createdInventory = inventoryService.createInventory(inventory);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdInventory.getInventoryId().toString()).build();
        return Response.created(location).entity(createdInventory).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update inventory item", description = "Updates an existing inventory item")
    @APIResponse(
        responseCode = "200",
        description = "Inventory updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Inventory.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Inventory not found"
    )
    @APIResponse(
        responseCode = "409",
        description = "Another inventory record already exists for this product"
    )
    public Inventory updateInventory(
        @Parameter(description = "ID of the inventory item", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "Updated inventory details", required = true)
        @NotNull @Valid Inventory inventory) {
        return inventoryService.updateInventory(id, inventory);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete inventory item", description = "Deletes an inventory item")
    @APIResponse(
        responseCode = "204",
        description = "Inventory deleted"
    )
    @APIResponse(
        responseCode = "404",
        description = "Inventory not found"
    )
    public Response deleteInventory(
        @Parameter(description = "ID of the inventory item", required = true)
        @PathParam("id") Long id) {
        inventoryService.deleteInventory(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/product/{productId}/quantity/{quantity}")
    @Operation(summary = "Update product quantity", description = "Updates the quantity for a specific product")
    @APIResponse(
        responseCode = "200",
        description = "Quantity updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Inventory.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Inventory not found for product"
    )
    public Inventory updateQuantity(
        @Parameter(description = "Product ID", required = true)
        @PathParam("productId") Long productId,
        @Parameter(description = "New quantity", required = true)
        @PathParam("quantity") int quantity) {
        return inventoryService.updateQuantity(productId, quantity);
    }

    @PATCH
    @Path("/product/{productId}/reserve/{quantity}")
    @Operation(summary = "Reserve inventory for a product", 
               description = "Reserves the specified quantity of inventory for a product if it's available in the catalog")
    @APIResponse(
        responseCode = "200",
        description = "Inventory reserved successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Inventory.class)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid quantity or insufficient inventory available"
    )
    @APIResponse(
        responseCode = "404",
        description = "Product not found in catalog or inventory not found"
    )
    public Inventory reserveInventory(
        @Parameter(description = "Product ID", required = true)
        @PathParam("productId") Long productId,
        @Parameter(description = "Quantity to reserve", required = true)
        @PathParam("quantity") int quantity) {
        return inventoryService.reserveInventory(productId, quantity);
    }

    @GET
    @Path("/product-info/{productId}")
    @Operation(summary = "Get product information using custom RestClientBuilder", 
               description = "Demonstrates advanced RestClientBuilder usage with custom timeout configuration")
    @APIResponse(
        responseCode = "200",
        description = "Product information retrieved successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Product.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Product not found"
    )
    public Response getProductInfo(
        @Parameter(description = "Product ID", required = true)
        @PathParam("productId") Long productId) {
        
        Product product = inventoryService.getProductWithCustomClient(productId);
        if (product != null) {
            return Response.ok(product).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Product not found\"}")
                    .build();
        }
    }
}
