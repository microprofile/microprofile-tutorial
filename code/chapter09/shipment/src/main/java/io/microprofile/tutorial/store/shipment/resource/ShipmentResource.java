package io.microprofile.tutorial.store.shipment.resource;

import io.microprofile.tutorial.store.shipment.entity.Shipment;
import io.microprofile.tutorial.store.shipment.entity.ShipmentStatus;
import io.microprofile.tutorial.store.shipment.service.ShipmentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * REST resource for shipment operations.
 */
@Path("/api/shipments")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Shipment Resource", description = "Operations for managing shipments")
public class ShipmentResource {

    private static final Logger LOGGER = Logger.getLogger(ShipmentResource.class.getName());

    @Inject
    private ShipmentService shipmentService;

    /**
     * Creates a new shipment for an order.
     *
     * @param orderId The order ID
     * @return The created shipment
     */
    @POST
    @Path("/orders/{orderId}")
    @Operation(summary = "Create a new shipment for an order")
    @APIResponse(responseCode = "201", description = "Shipment created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "400", description = "Invalid order ID")
    @APIResponse(responseCode = "404", description = "Order not found or not ready for shipment")
    public Response createShipment(
            @Parameter(description = "Order ID", required = true)
            @PathParam("orderId") Long orderId) {
        
        LOGGER.info("REST request to create shipment for order: " + orderId);
        
        Shipment shipment = shipmentService.createShipment(orderId);
        if (shipment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Order not found or not ready for shipment\"}")
                    .build();
        }
        
        return Response.status(Response.Status.CREATED)
                .entity(shipment)
                .build();
    }

    /**
     * Gets a shipment by ID.
     *
     * @param shipmentId The shipment ID
     * @return The shipment
     */
    @GET
    @Path("/{shipmentId}")
    @Operation(summary = "Get a shipment by ID")
    @APIResponse(responseCode = "200", description = "Shipment found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response getShipment(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId) {
        
        LOGGER.info("REST request to get shipment: " + shipmentId);
        
        Optional<Shipment> shipment = shipmentService.getShipment(shipmentId);
        if (shipment.isPresent()) {
            return Response.ok(shipment.get()).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }

    /**
     * Gets all shipments.
     *
     * @return All shipments
     */
    @GET
    @Operation(summary = "Get all shipments")
    @APIResponse(responseCode = "200", description = "All shipments",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Shipment.class)))
    public Response getAllShipments() {
        LOGGER.info("REST request to get all shipments");
        
        List<Shipment> shipments = shipmentService.getAllShipments();
        return Response.ok(shipments).build();
    }

    /**
     * Gets shipments by status.
     *
     * @param status The status
     * @return The shipments with the given status
     */
    @GET
    @Path("/status/{status}")
    @Operation(summary = "Get shipments by status")
    @APIResponse(responseCode = "200", description = "Shipments with the given status",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Shipment.class)))
    public Response getShipmentsByStatus(
            @Parameter(description = "Shipment status", required = true)
            @PathParam("status") ShipmentStatus status) {
        
        LOGGER.info("REST request to get shipments with status: " + status);
        
        List<Shipment> shipments = shipmentService.getShipmentsByStatus(status);
        return Response.ok(shipments).build();
    }

    /**
     * Gets shipments by order ID.
     *
     * @param orderId The order ID
     * @return The shipments for the given order
     */
    @GET
    @Path("/orders/{orderId}")
    @Operation(summary = "Get shipments by order ID")
    @APIResponse(responseCode = "200", description = "Shipments for the given order",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Shipment.class)))
    public Response getShipmentsByOrder(
            @Parameter(description = "Order ID", required = true)
            @PathParam("orderId") Long orderId) {
        
        LOGGER.info("REST request to get shipments for order: " + orderId);
        
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(orderId);
        return Response.ok(shipments).build();
    }

    /**
     * Gets a shipment by tracking number.
     *
     * @param trackingNumber The tracking number
     * @return The shipment
     */
    @GET
    @Path("/tracking/{trackingNumber}")
    @Operation(summary = "Get a shipment by tracking number")
    @APIResponse(responseCode = "200", description = "Shipment found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response getShipmentByTrackingNumber(
            @Parameter(description = "Tracking number", required = true)
            @PathParam("trackingNumber") String trackingNumber) {
        
        LOGGER.info("REST request to get shipment with tracking number: " + trackingNumber);
        
        Optional<Shipment> shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        if (shipment.isPresent()) {
            return Response.ok(shipment.get()).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }

    /**
     * Updates the status of a shipment.
     *
     * @param shipmentId The shipment ID
     * @param status The new status
     * @return The updated shipment
     */
    @PUT
    @Path("/{shipmentId}/status/{status}")
    @Operation(summary = "Update shipment status")
    @APIResponse(responseCode = "200", description = "Shipment status updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response updateShipmentStatus(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId,
            @Parameter(description = "New status", required = true)
            @PathParam("status") ShipmentStatus status) {
        
        LOGGER.info("REST request to update shipment " + shipmentId + " status to " + status);
        
        Optional<Shipment> shipment = shipmentService.updateShipmentStatus(shipmentId, status);
        if (shipment.isPresent()) {
            return Response.ok(shipment.get()).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }

    /**
     * Updates the carrier for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param carrier The new carrier
     * @return The updated shipment
     */
    @PUT
    @Path("/{shipmentId}/carrier")
    @Operation(summary = "Update shipment carrier")
    @APIResponse(responseCode = "200", description = "Carrier updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response updateCarrier(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId,
            @Parameter(description = "New carrier", required = true)
            @NotNull String carrier) {
        
        LOGGER.info("REST request to update carrier for shipment " + shipmentId + " to " + carrier);
        
        Optional<Shipment> shipment = shipmentService.updateCarrier(shipmentId, carrier);
        if (shipment.isPresent()) {
            return Response.ok(shipment.get()).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }

    /**
     * Updates the tracking number for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param trackingNumber The new tracking number
     * @return The updated shipment
     */
    @PUT
    @Path("/{shipmentId}/tracking")
    @Operation(summary = "Update shipment tracking number")
    @APIResponse(responseCode = "200", description = "Tracking number updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response updateTrackingNumber(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId,
            @Parameter(description = "New tracking number", required = true)
            @NotNull String trackingNumber) {
        
        LOGGER.info("REST request to update tracking number for shipment " + shipmentId + " to " + trackingNumber);
        
        Optional<Shipment> shipment = shipmentService.updateTrackingNumber(shipmentId, trackingNumber);
        if (shipment.isPresent()) {
            return Response.ok(shipment.get()).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }

    /**
     * Updates the estimated delivery date for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param dateStr The new estimated delivery date (ISO format)
     * @return The updated shipment
     */
    @PUT
    @Path("/{shipmentId}/delivery-date")
    @Operation(summary = "Update shipment estimated delivery date")
    @APIResponse(responseCode = "200", description = "Estimated delivery date updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "400", description = "Invalid date format")
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response updateEstimatedDelivery(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId,
            @Parameter(description = "New estimated delivery date (ISO format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @NotNull String dateStr) {
        
        LOGGER.info("REST request to update estimated delivery for shipment " + shipmentId + " to " + dateStr);
        
        try {
            LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Optional<Shipment> shipment = shipmentService.updateEstimatedDelivery(shipmentId, date);
            
            if (shipment.isPresent()) {
                return Response.ok(shipment.get()).build();
            }
            
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Shipment not found\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid date format. Use ISO format: yyyy-MM-dd'T'HH:mm:ss\"}")
                    .build();
        }
    }

    /**
     * Updates the notes for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param notes The new notes
     * @return The updated shipment
     */
    @PUT
    @Path("/{shipmentId}/notes")
    @Operation(summary = "Update shipment notes")
    @APIResponse(responseCode = "200", description = "Notes updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, 
                    schema = @Schema(implementation = Shipment.class)))
    @APIResponse(responseCode = "404", description = "Shipment not found")
    public Response updateNotes(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId,
            @Parameter(description = "New notes", required = true)
            String notes) {
        
        LOGGER.info("REST request to update notes for shipment " + shipmentId);
        
        Optional<Shipment> shipment = shipmentService.updateNotes(shipmentId, notes);
        if (shipment.isPresent()) {
            return Response.ok(shipment.get()).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }

    /**
     * Deletes a shipment.
     *
     * @param shipmentId The shipment ID
     * @return A response indicating success or failure
     */
    @DELETE
    @Path("/{shipmentId}")
    @Operation(summary = "Delete a shipment")
    @APIResponse(responseCode = "204", description = "Shipment deleted")
    @APIResponse(responseCode = "404", description = "Shipment not found")
    @APIResponse(responseCode = "400", description = "Shipment cannot be deleted due to its status")
    public Response deleteShipment(
            @Parameter(description = "Shipment ID", required = true)
            @PathParam("shipmentId") Long shipmentId) {
        
        LOGGER.info("REST request to delete shipment: " + shipmentId);
        
        boolean deleted = shipmentService.deleteShipment(shipmentId);
        if (deleted) {
            return Response.noContent().build();
        }
        
        // Check if shipment exists but cannot be deleted due to its status
        Optional<Shipment> shipment = shipmentService.getShipment(shipmentId);
        if (shipment.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Shipment cannot be deleted due to its status\"}")
                    .build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Shipment not found\"}")
                .build();
    }
}
