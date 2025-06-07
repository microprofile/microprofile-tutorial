package io.microprofile.tutorial.store.user.resource;

import io.microprofile.tutorial.store.user.entity.User;
import io.microprofile.tutorial.store.user.service.UserService;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.security.Principal;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for user management operations.
 * Provides endpoints for creating, retrieving, updating, and deleting users.
 * Implements standard RESTful practices with proper status codes and hypermedia links.
 */
@Path("/users")
@Tag(name = "User Management", description = "Operations for managing users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserService userService;
    
    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @APIResponse(responseCode = "200", description = "List of users")
    @APIResponse(responseCode = "204", description = "No users found")
    public Response getAllUsers() {
        List<User> users = userService.getAllUsers();
        
        if (users.isEmpty()) {
            return Response.noContent().build();
        }
        
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns a user by their ID")
    @APIResponse(responseCode = "200", description = "User found")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response getUserById(
            @PathParam("id") 
            @Parameter(description = "User ID", required = true) 
            Long id) {
        User user = userService.getUserById(id);
        // Add HATEOAS links
        URI selfLink = uriInfo.getBaseUriBuilder()
                            .path(UserResource.class)
                            .path(String.valueOf(user.getUserId()))
                            .build();
        return Response.ok(user)
                 .link(selfLink, "self")
                 .build();
    }

    @POST
    @Operation(summary = "Create new user", description = "Creates a new user")
    @APIResponse(responseCode = "201", description = "User created successfully")
    @APIResponse(responseCode = "400", description = "Invalid user data")
    @APIResponse(responseCode = "409", description = "Email already in use")
    public Response createUser(
            @Valid 
            @NotNull(message = "Request body cannot be empty")
            @Parameter(description = "User to create", required = true)
            User user) {
        User createdUser = userService.createUser(user);
        URI location = uriInfo.getAbsolutePathBuilder()
                          .path(String.valueOf(createdUser.getUserId()))
                          .build();
        return Response.created(location)
                 .entity(createdUser)
                 .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @APIResponse(responseCode = "200", description = "User updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid user data")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "409", description = "Email already in use")
    public Response updateUser(
            @PathParam("id") 
            @Parameter(description = "User ID", required = true)
            Long id, 
            
            @Valid 
            @NotNull(message = "Request body cannot be empty")
            @Parameter(description = "Updated user information", required = true)
            User user) {
        User updatedUser = userService.updateUser(id, user);
        return Response.ok(updatedUser).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID")
    @APIResponse(responseCode = "204", description = "User successfully deleted")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response deleteUser(
            @PathParam("id") 
            @Parameter(description = "User ID to delete", required = true)
            Long id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/profile")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the authenticated user")
    @APIResponse(responseCode = "200", description = "User profile retrieved successfully")
    @APIResponse(responseCode = "401", description = "User not authenticated")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response getUserProfile(@Context SecurityContext securityContext) {
        // Check if user is authenticated
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                         .entity("User not authenticated")
                         .build();
        }
        
        // Get the authenticated user's name/identifier
        String username = principal.getName();
        
        try {
            // Try to parse as user ID first (if the principal name is a numeric ID)
            try {
                Long userId = Long.parseLong(username);
                User user = userService.getUserById(userId);
                
                // Add HATEOAS links for the profile
                URI selfLink = uriInfo.getBaseUriBuilder()
                                    .path(UserResource.class)
                                    .path("profile")
                                    .build();
                
                return Response.ok(user)
                             .link(selfLink, "self")
                             .build();
                             
            } catch (NumberFormatException e) {
                // If username is not numeric, treat it as email and look up user by email
                User user = userService.getUserByEmail(username);
                
                // Add HATEOAS links for the profile
                URI selfLink = uriInfo.getBaseUriBuilder()
                                    .path(UserResource.class)
                                    .path("profile")
                                    .build();
                
                return Response.ok(user)
                             .link(selfLink, "self")
                             .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                         .entity("User profile not found")
                         .build();
        }
    }

    @GET
    @Path("/jwt")
    @Operation(summary = "Get JWT token", description = "Demonstrates JWT token creation")
    @APIResponse(responseCode = "200", description = "JWT token retrieved successfully")
    @APIResponse(responseCode = "401", description = "User not authenticated")
    public Response getJwtToken(@Context SecurityContext securityContext) {
        // Check if user is authenticated
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                         .entity("User not authenticated")
                         .build();
        }
        
        // For demonstration, we'll just return a dummy JWT token
        // In a real application, you would create a token based on the user's information
        String dummyToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                          + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ."
                          + "SflKxwRJSMeJK7y6gG7hP4Z7G7g";
        
        return Response.ok(dummyToken).build();
    }

    @GET
    @Path("/user-profile")
    @Operation(summary = "Simple JWT user profile", description = "A simple example showing basic JWT claim extraction for learning MicroProfile JWT")
    @APIResponse(responseCode = "200", description = "User profile with JWT claims extracted successfully")
    @APIResponse(responseCode = "401", description = "User not authenticated or not using JWT")
    public String getJwtUserProfile(@Context SecurityContext ctx) {
        // This is a simplified JWT demonstration for educational purposes
        try {
            // Cast the principal to JsonWebToken (only works with JWT authentication)
            JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
            
            if (jwt == null) {
                return "JWT token required for this endpoint";
            }
            
            String userId = jwt.getName(); // Extracts the "sub" claim
            Set<String> roles = jwt.getGroups(); // Extracts the "groups" claim
            String tenant = jwt.getClaim("tenant_id"); // Custom claim example
            
            return "User: " + userId + ", Roles: " + roles + ", Tenant: " + tenant;
                         
        } catch (ClassCastException e) {
            return "This endpoint requires JWT authentication (not other auth types)";
        }
    }
}
