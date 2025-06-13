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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;
import java.util.UUID;

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

    @POST
    @Path("/payments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Process payment with full details", description = "Process payment with comprehensive telemetry tracing")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Payment processed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid payment details"),
        @APIResponse(responseCode = "500", description = "Payment processing failed")
    })
    public Response processPaymentWithDetails(PaymentDetails paymentDetails) 
        throws PaymentProcessingException {
        
        try {
            // Use PaymentService with full fault tolerance and telemetry
            CompletionStage<String> result = paymentService.processPayment(paymentDetails);
            String paymentResult = result.toCompletableFuture().get();
            
            return Response.ok(paymentResult, MediaType.APPLICATION_JSON).build();
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        }
    }

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Verify payment with telemetry", description = "Comprehensive payment verification with distributed tracing")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Payment verified successfully"),
        @APIResponse(responseCode = "400", description = "Payment verification failed"),
        @APIResponse(responseCode = "500", description = "Verification process error")
    })
    public Response verifyPaymentWithTelemetry(PaymentDetails paymentDetails) 
        throws PaymentProcessingException {
        
        try {
            // Generate a unique transaction ID for this verification
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Use the new telemetry-enabled verification method
            CompletionStage<String> result = paymentService.verifyPaymentWithTelemetry(paymentDetails, transactionId);
            String verificationResult = result.toCompletableFuture().get();
            
            return Response.ok(verificationResult, MediaType.APPLICATION_JSON).build();
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Payment verification failed: " + e.getMessage());
        }
    }

}
