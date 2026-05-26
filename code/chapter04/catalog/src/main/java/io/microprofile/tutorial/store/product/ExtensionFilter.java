package io.microprofile.tutorial.store.product;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;

/**
 * OpenAPI filter demonstrating the use of getExtension() and hasExtension() methods.
 * This filter is called for each element in the OpenAPI model tree.
 */
public class ExtensionFilter implements OASFilter {
    
    @Override
    public Operation filterOperation(Operation operation) {
        // Check if a custom extension exists using the new hasExtension method
        if (operation.hasExtension("x-custom-timeout")) {
            // Retrieve the extension value using the new getExtension method
            Object timeout = operation.getExtension("x-custom-timeout");
            System.out.println("Custom timeout found: " + timeout);
            
            // Modify based on the extension
            if (timeout != null && timeout instanceof Integer && (Integer) timeout > 30) {
                operation.addExtension("x-requires-approval", true);
            }
        }
        
        // Check for rate limiting extension
        if (operation.hasExtension("x-rate-limit")) {
            Object rateLimit = operation.getExtension("x-rate-limit");
            System.out.println("Rate limit: " + rateLimit);
        }
        
        return operation;
    }
}
