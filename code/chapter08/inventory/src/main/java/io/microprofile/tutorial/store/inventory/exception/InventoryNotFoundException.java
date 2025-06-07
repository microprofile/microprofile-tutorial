package io.microprofile.tutorial.store.inventory.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when an inventory item is not found.
 */
public class InventoryNotFoundException extends RuntimeException {
    private Response.Status status;
    
    /**
     * Constructs a new InventoryNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public InventoryNotFoundException(String message) {
        super(message);
        this.status = Response.Status.NOT_FOUND;
    }
    
    /**
     * Constructs a new InventoryNotFoundException with the specified message and status.
     *
     * @param message the detail message
     * @param status the HTTP status code to return
     */
    public InventoryNotFoundException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }
    
    /**
     * Gets the HTTP status associated with this exception.
     *
     * @return the HTTP status
     */
    public Response.Status getStatus() {
        return status;
    }
}
