package io.microprofile.tutorial.store.inventory;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * JAX-RS application for inventory management.
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Inventory API",
        version = "1.0.0",
        description = "API for managing product inventory",
        license = @License(
            name = "Eclipse Public License 2.0",
            url = "https://www.eclipse.org/legal/epl-2.0/"),
        contact = @Contact(
            name = "Inventory API Support",
            email = "support@example.com")),
    tags = {
        @Tag(name = "Inventory", description = "Operations related to product inventory management")
    }
)
public class InventoryApplication extends Application {
    // The resources will be discovered automatically
}
