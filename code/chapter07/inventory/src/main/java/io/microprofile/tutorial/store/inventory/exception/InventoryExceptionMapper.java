package io.microprofile.tutorial.store.inventory.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exception mapper for handling all runtime exceptions in the inventory service.
 * Maps exceptions to appropriate HTTP responses with formatted error messages.
 */
@Provider
public class InventoryExceptionMapper implements ExceptionMapper<RuntimeException> {
    
    private static final Logger LOGGER = Logger.getLogger(InventoryExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof InventoryNotFoundException) {
            InventoryNotFoundException notFoundException = (InventoryNotFoundException) exception;
            LOGGER.log(Level.INFO, "Resource not found: {0}", exception.getMessage());
            
            return Response.status(notFoundException.getStatus())
                    .entity(new ErrorResponse("not_found", exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } else if (exception instanceof InventoryConflictException) {
            InventoryConflictException conflictException = (InventoryConflictException) exception;
            LOGGER.log(Level.INFO, "Resource conflict: {0}", exception.getMessage());
            
            return Response.status(conflictException.getStatus())
                    .entity(new ErrorResponse("conflict", exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        
        // Handle unexpected exceptions
        LOGGER.log(Level.SEVERE, "Unexpected error", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("server_error", "An unexpected error occurred"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
