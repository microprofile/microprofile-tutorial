package io.microprofile.tutorial.store.inventory.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an error response to be returned to the client.
 * Used for formatting error messages in a consistent way.
 */
public class ErrorResponse {
    private String errorCode;
    private String message;
    private Map<String, Object> details;
    
    /**
     * Constructs a new ErrorResponse with the specified error code and message.
     *
     * @param errorCode a code identifying the error type
     * @param message a human-readable error message
     */
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = new HashMap<>();
    }
    
    /**
     * Constructs a new ErrorResponse with the specified error code, message, and details.
     *
     * @param errorCode a code identifying the error type
     * @param message a human-readable error message
     * @param details additional information about the error
     */
    public ErrorResponse(String errorCode, String message, Map<String, Object> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }
    
    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Sets the error code.
     *
     * @param errorCode the error code to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the error message.
     *
     * @param message the error message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the error details.
     *
     * @return the error details
     */
    public Map<String, Object> getDetails() {
        return details;
    }
    
    /**
     * Sets the error details.
     *
     * @param details the error details to set
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    /**
     * Adds a detail to the error response.
     *
     * @param key the detail key
     * @param value the detail value
     */
    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
}
