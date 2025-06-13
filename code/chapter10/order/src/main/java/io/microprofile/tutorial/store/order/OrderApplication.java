package io.microprofile.tutorial.store.order;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * JAX-RS Application class that defines the base path for all REST endpoints.
 * Also contains OpenAPI annotations for API documentation and JWT security scheme.
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Order Management Service",
        version = "1.0.0",
        description = "A microservice for managing orders in the MicroProfile E-Commerce Store. " +
                     "Provides comprehensive order management including creation, updates, status tracking, " +
                     "and customer order retrieval with full CRUD operations."
    ),
    servers = {
        @Server(url = "/order", description = "Order Management Service")
    }
)
@LoginConfig(authMethod = "MP-JWT")
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT authentication with bearer token"
)
public class OrderApplication extends Application {
    // No additional configuration is needed here
    // JAX-RS will automatically discover and register resources
}
