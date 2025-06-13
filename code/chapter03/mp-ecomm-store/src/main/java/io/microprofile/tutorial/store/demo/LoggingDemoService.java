package io.microprofile.tutorial.store.demo;

import io.microprofile.tutorial.store.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * A demonstration class showing how to apply the @Logged interceptor to individual methods
 * instead of the entire class.
 */
@ApplicationScoped
public class LoggingDemoService {
    
    // This method will be logged because of the @Logged annotation
    @Logged
    public String loggedMethod(String input) {
        // Method logic
        return "Processed: " + input;
    }
    
    // This method will NOT be logged since it doesn't have the @Logged annotation
    public String nonLoggedMethod(String input) {
        // Method logic
        return "Silently processed: " + input;
    }
    
    /**
     * Example of a method with exception that will be logged
     */
    @Logged
    public void methodWithException() throws Exception {
        throw new Exception("This exception will be logged by the interceptor");
    }
}
