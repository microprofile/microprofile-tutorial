package io.microprofile.tutorial.store.payment.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;
import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.microprofile.tutorial.store.payment.service.PaymentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
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

            String paymentResult = paymentService.authorizePayment(paymentDetails);
            
            return Response.ok(paymentResult, MediaType.APPLICATION_JSON).build();
            
        } catch (PaymentProcessingException e) {
            // Re-throw to let fault tolerance handle it
            throw e;
        } catch (Exception e) {
            // Handle other exceptions
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        }
    }

    @GET
    @Path("/health/gateway")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Check gateway health", description = "Check payment gateway health with Circuit Breaker and Timeout protection")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Gateway is healthy"),
        @APIResponse(responseCode = "503", description = "Gateway is unhealthy or circuit breaker is OPEN")
    })
    public Response checkGatewayHealth() {
        try {
            paymentService.checkGatewayHealth();
            return Response.ok(Json.createObjectBuilder()
                .add("status", "healthy")
                .add("gateway", endpoint)
                .build()).build();
        } catch (CircuitBreakerOpenException e) {
            return Response.status(503)
                .entity(Json.createObjectBuilder()
                    .add("status", "circuit_open")
                    .add("message", "Circuit breaker is OPEN — gateway requests blocked")
                    .build())
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            return Response.status(503)
                .entity(Json.createObjectBuilder()
                    .add("status", "unhealthy")
                    .add("message", e.getMessage())
                    .build())
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    @POST
    @Path("/notify/{paymentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Send payment notification", description = "Send async payment notification with Bulkhead and Timeout protection")
    @APIResponses(value = {
        @APIResponse(responseCode = "202", description = "Notification accepted for processing"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public CompletionStage<Response> sendNotification(
        @PathParam("paymentId") String paymentId,
        @QueryParam("recipient") @DefaultValue("customer@example.com") String recipient
    ) {
        return paymentService.sendPaymentNotification(paymentId, recipient)
            .thenApply(message -> Response.accepted()
                .entity(Json.createObjectBuilder()
                    .add("status", "accepted")
                    .add("message", message)
                    .add("paymentId", paymentId)
                    .build())
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

}
