package io.microprofile.tutorial.store.inventory.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when there is a conflict with an inventory operation,
 * such as when trying to create an inventory for a product that already has one.
 */
public class InventoryConflictException extends RuntimeException {
    private Response.Status status;
    
    /**
     * Constructs a new InventoryConflictException with the specified message.
     *
     * @param message the detail message
     */
    public InventoryConflictException(String message) {
        super(message);
        this.status = Response.Status.CONFLICT;
    }
    
    /**
     * Constructs a new InventoryConflictException with the specified message and status.
     *
     * @param message the detail message
     * @param status the HTTP status code to return
     */
    public InventoryConflictException(String message, Response.Status status) {
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
