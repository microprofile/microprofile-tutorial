package io.microprofile.tutorial.payment.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;


@RequestScoped
@Path("/authorize")
public class PaymentService {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Process payment", description = "Process payment using the payment gateway API")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Payment processed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid input data"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response processPayment() {

        // Example logic to call the payment gateway API
        System.out.println();
        System.out.println("Calling payment gateway API to process payment...");
        // Here, assume a successful payment operation for demonstration purposes
        // Actual implementation would involve calling the payment gateway and handling the response
        
        // Dummy response for successful payment processing
        String result = "{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}";
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }
}
