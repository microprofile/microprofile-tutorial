package io.microprofile.tutorial.store.user;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

/**
 * JAX-RS Application class that defines the base path for all REST endpoints.
 * Also contains OpenAPI annotations for API documentation.
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "User Management API",
        version = "1.0.0",
        description = "REST API for managing user profiles and authentication"
    ),
    servers = {
        @Server(url = "/user", description = "User Management Service")
    }
)
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT token authentication"
)
@LoginConfig(authMethod = "MP-JWT")
public class UserApplication extends Application {
    // Empty class body is sufficient for JAX-RS to work
}
