package io.microprofile.tutorial.store.shoppingcart.resource;

import io.microprofile.tutorial.store.shoppingcart.entity.CartItem;
import io.microprofile.tutorial.store.shoppingcart.entity.ShoppingCart;
import io.microprofile.tutorial.store.shoppingcart.service.ShoppingCartService;

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

import java.net.URI;
import java.util.List;

/**
 * REST resource for shopping cart operations.
 */
@Path("/carts")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Shopping Cart Resource", description = "Shopping cart management operations")
public class ShoppingCartResource {

    @Inject
    private ShoppingCartService cartService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Get all shopping carts", description = "Returns a list of all shopping carts")
    @APIResponse(
        responseCode = "200",
        description = "List of shopping carts",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = ShoppingCart.class)
        )
    )
    public List<ShoppingCart> getAllCarts() {
        return cartService.getAllCarts();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get cart by ID", description = "Returns a specific shopping cart by ID")
    @APIResponse(
        responseCode = "200",
        description = "Shopping cart",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = ShoppingCart.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart not found"
    )
    public ShoppingCart getCartById(
        @Parameter(description = "ID of the cart", required = true)
        @PathParam("id") Long cartId) {
        return cartService.getCartById(cartId);
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get cart by user ID", description = "Returns a user's shopping cart")
    @APIResponse(
        responseCode = "200",
        description = "Shopping cart",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = ShoppingCart.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart not found for user"
    )
    public Response getCartByUserId(
        @Parameter(description = "ID of the user", required = true)
        @PathParam("userId") Long userId) {
        try {
            ShoppingCart cart = cartService.getCartByUserId(userId);
            return Response.ok(cart).build();
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                // Create a new cart for the user
                ShoppingCart newCart = cartService.getOrCreateCart(userId);
                return Response.ok(newCart).build();
            }
            throw e;
        }
    }

    @POST
    @Path("/user/{userId}")
    @Operation(summary = "Create cart for user", description = "Creates a new shopping cart for a user")
    @APIResponse(
        responseCode = "201",
        description = "Cart created",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = ShoppingCart.class)
        )
    )
    public Response createCartForUser(
        @Parameter(description = "ID of the user", required = true)
        @PathParam("userId") Long userId) {
        ShoppingCart cart = cartService.getOrCreateCart(userId);
        URI location = uriInfo.getAbsolutePathBuilder().path(cart.getCartId().toString()).build();
        return Response.created(location).entity(cart).build();
    }

    @POST
    @Path("/{cartId}/items")
    @Operation(summary = "Add item to cart", description = "Adds an item to a shopping cart")
    @APIResponse(
        responseCode = "200",
        description = "Item added",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CartItem.class)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input or insufficient inventory"
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart not found"
    )
    public CartItem addItemToCart(
        @Parameter(description = "ID of the cart", required = true)
        @PathParam("cartId") Long cartId,
        @Parameter(description = "Item to add", required = true)
        @NotNull @Valid CartItem item) {
        return cartService.addItemToCart(cartId, item);
    }

    @PUT
    @Path("/{cartId}/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates an item in a shopping cart")
    @APIResponse(
        responseCode = "200",
        description = "Item updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CartItem.class)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input or insufficient inventory"
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart or item not found"
    )
    public CartItem updateCartItem(
        @Parameter(description = "ID of the cart", required = true)
        @PathParam("cartId") Long cartId,
        @Parameter(description = "ID of the item", required = true)
        @PathParam("itemId") Long itemId,
        @Parameter(description = "Updated item", required = true)
        @NotNull @Valid CartItem item) {
        return cartService.updateCartItem(cartId, itemId, item);
    }

    @DELETE
    @Path("/{cartId}/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from a shopping cart")
    @APIResponse(
        responseCode = "204",
        description = "Item removed"
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart or item not found"
    )
    public Response removeItemFromCart(
        @Parameter(description = "ID of the cart", required = true)
        @PathParam("cartId") Long cartId,
        @Parameter(description = "ID of the item", required = true)
        @PathParam("itemId") Long itemId) {
        cartService.removeItemFromCart(cartId, itemId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{cartId}/items")
    @Operation(summary = "Clear cart", description = "Removes all items from a shopping cart")
    @APIResponse(
        responseCode = "204",
        description = "Cart cleared"
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart not found"
    )
    public Response clearCart(
        @Parameter(description = "ID of the cart", required = true)
        @PathParam("cartId") Long cartId) {
        cartService.clearCart(cartId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{cartId}")
    @Operation(summary = "Delete cart", description = "Deletes a shopping cart")
    @APIResponse(
        responseCode = "204",
        description = "Cart deleted"
    )
    @APIResponse(
        responseCode = "404",
        description = "Cart not found"
    )
    public Response deleteCart(
        @Parameter(description = "ID of the cart", required = true)
        @PathParam("cartId") Long cartId) {
        cartService.deleteCart(cartId);
        return Response.noContent().build();
    }
}
