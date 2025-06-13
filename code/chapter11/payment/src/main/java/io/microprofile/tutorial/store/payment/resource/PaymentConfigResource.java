package io.microprofile.tutorial.store.payment.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import io.microprofile.tutorial.store.payment.config.PaymentConfig;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;

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
        Map<String, String> configValues = new HashMap<>();
        
        // Retrieve values from our custom ConfigSource
        configValues.put("gateway.endpoint", PaymentConfig.getConfigProperty("payment.gateway.endpoint"));
        
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
                    .entity("Both 'key' and 'value' must be provided").build();
        }
        
        // Only allow updating specific payment properties
        if (!key.startsWith("payment.")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Only payment configuration properties can be updated").build();
        }
        
        // Update the property in our custom ConfigSource
        PaymentConfig.updateProperty(key, value);
        
        return Response.ok(Map.of("message", "Configuration updated successfully", 
                "key", key, "value", value)).build();
    }
    
    /**
     * Example of how to use the payment configuration in a real payment processing method.
     * 
     * @param paymentDetails Payment details for processing
     * @return Response with payment result
     */
    @POST
    @Path("/process-example")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processPaymentExample(PaymentDetails paymentDetails) {
        // Using configuration values in payment processing logic
        String gatewayEndpoint = PaymentConfig.getConfigProperty("payment.gateway.endpoint");
        
        // This is just for demonstration - in a real implementation, 
        // we would use these values to configure the payment gateway client
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Payment processed successfully");
        result.put("amount", paymentDetails.getAmount());
        result.put("configUsed", Map.of(
                "gatewayEndpoint", gatewayEndpoint
        ));
        
        return Response.ok(result).build();
    }
}
