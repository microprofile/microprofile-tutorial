package io.microprofile.tutorial.store.payment.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;
import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.microprofile.tutorial.store.payment.service.PaymentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RequestScoped
@Path("/")
public class PaymentResource {
    
    @Inject
    @ConfigProperty(name = "payment.gateway.endpoint")
    private String endpoint;

    @Inject
    private PaymentService paymentService;

    @POST
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Process payment", description = "Process payment using the payment gateway API with fault tolerance")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Payment processed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid input data"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response processPayment(@QueryParam("amount") Double amount) 
        throws PaymentProcessingException, CriticalPaymentException {
        
        // Input validation
        if (amount == null || amount <= 0) {
            throw new CriticalPaymentException("Invalid payment amount: " + amount);
        }

        try {
            // Create PaymentDetails using constructor
            PaymentDetails paymentDetails = new PaymentDetails(
                "****-****-****-1111", // cardNumber - placeholder for demo
                "Demo User", // cardHolderName
                "12/25", // expiryDate
                "***", // securityCode
                BigDecimal.valueOf(amount) // amount
            );

            // Use PaymentService with full fault tolerance features
            CompletionStage<String> result = paymentService.processPayment(paymentDetails);
            
            // Wait for async result (in production, consider different patterns)
            String paymentResult = result.toCompletableFuture().get();
            
            return Response.ok(paymentResult, MediaType.APPLICATION_JSON).build();
            
        } catch (PaymentProcessingException e) {
            // Re-throw to let fault tolerance handle it
            throw e;
        } catch (Exception e) {
            // Handle other exceptions
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        }
    }

}
