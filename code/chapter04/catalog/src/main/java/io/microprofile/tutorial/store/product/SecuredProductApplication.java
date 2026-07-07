package io.microprofile.tutorial.store.product;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.License;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Application class demonstrating security scheme definitions in MicroProfile OpenAPI 4.1.
 * This shows how to document multiple security mechanisms for your API.
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Secured Product API",
        version = "1.0.0",
        description = "Product API with multiple security schemes demonstrating MicroProfile OpenAPI 4.1 capabilities",
        contact = @Contact(
            name = "API Support",
            email = "support@example.com",
            url = "https://example.com/support"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    )
)
@SecuritySchemes({
    @SecurityScheme(
        securitySchemeName = "apiKey",
        type = SecuritySchemeType.APIKEY,
        description = "API Key authentication - provide your API key in the X-API-Key header",
        in = SecuritySchemeIn.HEADER,
        apiKeyName = "X-API-Key"
    ),
    @SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        description = "JWT Bearer token authentication - obtain token from /auth/login endpoint",
        scheme = "bearer",
        bearerFormat = "JWT"
    ),
    @SecurityScheme(
        securitySchemeName = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        description = "OAuth2 authentication with authorization code flow",
        flows = @OAuthFlows(
            authorizationCode = @OAuthFlow(
                authorizationUrl = "https://example.com/oauth/authorize",
                tokenUrl = "https://example.com/oauth/token",
                refreshUrl = "https://example.com/oauth/refresh",
                scopes = {
                    @OAuthScope(
                        name = "read:products", 
                        description = "Read product information"
                    ),
                    @OAuthScope(
                        name = "write:products", 
                        description = "Create and modify product information"
                    ),
                    @OAuthScope(
                        name = "delete:products", 
                        description = "Delete product information"
                    )
                }
            )
        )
    ),
    @SecurityScheme(
        securitySchemeName = "basicAuth",
        type = SecuritySchemeType.HTTP,
        description = "Basic HTTP authentication",
        scheme = "basic"
    )
})
public class SecuredProductApplication extends Application {
}
