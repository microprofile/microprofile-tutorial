package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.entity.Payment;
import io.microprofile.tutorial.store.payment.entity.PaymentStatus;
import io.microprofile.tutorial.store.payment.service.PaymentService;

import java.net.URI;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for payment operations.
 */
@Path("/payments")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Payment Resource", description = "Payment management operations")
public class PaymentResource {

    @Inject
    private PaymentService paymentService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Operation(summary = "Get all payments", description = "Returns a list of all payments")
    @APIResponse(
        responseCode = "200",
        description = "List of payments",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Payment.class)
        )
    )
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get payment by ID", description = "Returns a specific payment by ID")
    @APIResponse(
        responseCode = "200",
        description = "Payment",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Payment.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Payment not found"
    )
    public Payment getPaymentById(
        @Parameter(description = "ID of the payment", required = true)
        @PathParam("id") Long id) {
        return paymentService.getPaymentById(id);
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get payments by user ID", description = "Returns payments for a specific user")
    @APIResponse(
        responseCode = "200",
        description = "List of payments",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Payment.class)
        )
    )
    public List<Payment> getPaymentsByUserId(
        @Parameter(description = "ID of the user", required = true)
        @PathParam("userId") Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @GET
    @Path("/order/{orderId}")
    @Operation(summary = "Get payments by order ID", description = "Returns payments for a specific order")
    @APIResponse(
        responseCode = "200",
        description = "List of payments",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Payment.class)
        )
    )
    public List<Payment> getPaymentsByOrderId(
        @Parameter(description = "ID of the order", required = true)
        @PathParam("orderId") Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Returns payments with a specific status")
    @APIResponse(
        responseCode = "200",
        description = "List of payments",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = Payment.class)
        )
    )
    public List<Payment> getPaymentsByStatus(
        @Parameter(description = "Status of the payments", required = true)
        @PathParam("status") String status) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            return paymentService.getPaymentsByStatus(paymentStatus);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid payment status: " + status, Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Operation(summary = "Create new payment", description = "Creates a new payment")
    @APIResponse(
        responseCode = "201",
        description = "Payment created",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Payment.class)
        )
    )
    public Response createPayment(
        @Parameter(description = "Payment details", required = true)
        @NotNull @Valid Payment payment) {
        Payment createdPayment = paymentService.createPayment(payment);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdPayment.getPaymentId().toString()).build();
        return Response.created(location).entity(createdPayment).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update payment", description = "Updates an existing payment")
    @APIResponse(
        responseCode = "200",
        description = "Payment updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Payment.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Payment not found"
    )
    public Payment updatePayment(
        @Parameter(description = "ID of the payment", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "Updated payment details", required = true)
        @NotNull @Valid Payment payment) {
        return paymentService.updatePayment(id, payment);
    }

    @PATCH
    @Path("/{id}/status/{status}")
    @Operation(summary = "Update payment status", description = "Updates the status of an existing payment")
    @APIResponse(
        responseCode = "200",
        description = "Payment status updated",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Payment.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Payment not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid payment status"
    )
    public Payment updatePaymentStatus(
        @Parameter(description = "ID of the payment", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "New status", required = true)
        @PathParam("status") String status) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            return paymentService.updatePaymentStatus(id, paymentStatus);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid payment status: " + status, Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("/{id}/process")
    @Operation(summary = "Process payment", description = "Processes an existing payment")
    @APIResponse(
        responseCode = "200",
        description = "Payment processed",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Payment.class)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Payment not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Payment is not in PENDING state"
    )
    public Payment processPayment(
        @Parameter(description = "ID of the payment", required = true)
        @PathParam("id") Long id) {
        return paymentService.processPayment(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete payment", description = "Deletes a payment")
    @APIResponse(
        responseCode = "204",
        description = "Payment deleted"
    )
    @APIResponse(
        responseCode = "404",
        description = "Payment not found"
    )
    public Response deletePayment(
        @Parameter(description = "ID of the payment", required = true)
        @PathParam("id") Long id) {
        paymentService.deletePayment(id);
        return Response.noContent().build();
    }
}
