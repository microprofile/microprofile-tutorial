package io.microprofile.tutorial.store.payment.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
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
    private PaymentService paymentService;

    @POST
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Process payment", description = "Process payment using the payment gateway API with fault tolerance")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Payment processed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = String.class)))
    })
    public CompletionStage<Response> processPayment(@QueryParam("amount") Double amount) {

        if (amount == null || amount <= 0) {
            return CompletableFuture.completedFuture(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\", \"message\":\"Invalid payment amount: " + amount + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build()
            );
        }

        PaymentDetails paymentDetails = new PaymentDetails(
            "****-****-****-1111",
            "Demo User",
            "12/25",
            "***",
            BigDecimal.valueOf(amount)
        );

        System.out.println("[" + Thread.currentThread().getName() + "] HTTP request received, handing off to managed executor");

        try {
            return paymentService.processPayment(paymentDetails)
                .thenApply(result -> {
                    System.out.println("[" + Thread.currentThread().getName() + "] Sending HTTP response for amount: " + amount);
                    return Response.ok(result, MediaType.APPLICATION_JSON).build();
                })
                .exceptionally(e -> {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    System.out.println("[" + Thread.currentThread().getName() + "] Error for amount " + amount + ": " + cause.getMessage());
                    return Response.ok(cause.getMessage(), MediaType.APPLICATION_JSON).build();
                });
        } catch (PaymentProcessingException e) {
            return CompletableFuture.completedFuture(
                Response.serverError()
                    .entity("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build()
            );
        }
    }
}
