package io.microprofile.tutorial.store.payment.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/authorize")
@RequestScoped
public class PaymentResource {
    
    @Inject
    @ConfigProperty(name = "payment.gateway.apiKey", defaultValue = "default_api_key")
    private String apiKey;

    @Inject
    @ConfigProperty(name = "payment.gateway.endpoint", defaultValue = "https://defaultapi.paymentgateway.com")
    private String endpoint;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "process payment", description = "Processes payment using a payment gateway")
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Payment processed successfully",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "400",
            description = "Payment processing failed",
            content = @Content(mediaType = "application/json")
        )
    })
    public Response processPayment(PaymentDetails paymentDetails) {

        // Example logic to call the payment gateway API
        System.out.println();
        System.out.println("Calling payment gateway API at: " + endpoint + " with API key: " + apiKey);
        // Here, assume a successful payment operation for demonstration purposes
        // Actual implementation would involve calling the payment gateway and handling the response
        
        // Dummy response for successful payment processing
        String result = "{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}";
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }
}
