package io.microprofile.tutorial.store.payment.resource;

import java.util.concurrent.CompletableFuture;

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
import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.microprofile.tutorial.store.payment.service.PaymentService;

@Path("/authorize")
@RequestScoped
public class PaymentResource {
    
    @Inject
    private PaymentService paymentService;

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
    public Response processPayment(PaymentDetails paymentDetails) throws PaymentProcessingException{
        try {
            CompletableFuture<String> result = paymentService.processPayment(paymentDetails);
            return Response.ok(result, MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"failed\", \"message\":\"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
