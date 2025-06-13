package io.microprofile.tutorial.store.user.resource;

import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

/**
 * REST resource for user management operations.
 * Provides endpoints for creating, retrieving, updating, and deleting users.
 * Implements standard RESTful practices with proper status codes and hypermedia links.
 */
@Tag(name = "User Management", description = "Operations for managing users")
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT authentication with bearer token"
)
public class UserResource {


    @Operation(
        summary = "Get user profile",
        description = "Returns the authenticated user's profile information extracted from the JWT token."
    )
    @SecurityRequirement(name = "jwt")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User profile returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token is missing or invalid"
        )
    })
    @GET
    @Path("/user-profile")
    public String getUserProfile(@Context SecurityContext ctx) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        String userId = jwt.getName(); // Extracts the "sub" claim
        Set<String> roles = jwt.getGroups(); // Extracts the "groups" claim
        String tenant = jwt.getClaim("tenant_id"); // Custom claim

        return "User: " + userId + ", Roles: " + roles + ", Tenant: " + tenant;
    }  
}
