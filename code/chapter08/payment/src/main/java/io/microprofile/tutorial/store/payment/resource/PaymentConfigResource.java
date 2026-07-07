package io.microprofile.tutorial.store.payment.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import io.microprofile.tutorial.store.payment.config.PaymentConfig;

/**
 * Resource to demonstrate the use of the custom ConfigSource.
 */
@ApplicationScoped
@Path("/payment-config")
public class PaymentConfigResource {
    
    /**
     * Get all payment configuration properties.
     * 
     * @return Response with payment configuration
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPaymentConfig() {
        JsonObject configValues = Json.createObjectBuilder()
            .add("gateway.endpoint", PaymentConfig.getConfigProperty("payment.gateway.endpoint"))
            .build();
        return Response.ok(configValues).build();
    }
    
    /**
     * Update a payment configuration property.
     * 
     * @param configUpdate Map containing the key and value to update
     * @return Response indicating success
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePaymentConfig(Map<String, String> configUpdate) {
        String key = configUpdate.get("key");
        String value = configUpdate.get("value");
        
        if (key == null || value == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                        .add("error", "Both 'key' and 'value' must be provided")
                        .build()).build();
        }

        // Only allow updating specific payment properties
        if (!key.startsWith("payment.")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                        .add("error", "Only payment configuration properties can be updated")
                        .build()).build();
        }

        PaymentConfig.updateProperty(key, value);

        return Response.ok(Json.createObjectBuilder()
                .add("message", "Configuration updated successfully")
                .add("key", key)
                .add("value", value)
                .build()).build();
    }
    
}
