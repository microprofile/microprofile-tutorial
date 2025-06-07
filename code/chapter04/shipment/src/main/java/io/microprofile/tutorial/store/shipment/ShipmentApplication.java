package io.microprofile.tutorial.store.shipment;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * JAX-RS Application class for the shipment service.
 */
@ApplicationPath("/")
@OpenAPIDefinition(
        info = @Info(
                title = "Shipment Service API",
                version = "1.0.0",
                description = "API for managing shipments in the microprofile tutorial store",
                contact = @Contact(
                        name = "Shipment Service Support",
                        email = "shipment@example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        tags = {
                @Tag(name = "Shipment Resource", description = "Operations for managing shipments")
        }
)
public class ShipmentApplication extends Application {
    // Empty application class, all configuration is provided by annotations
}
