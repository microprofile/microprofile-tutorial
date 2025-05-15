package io.microprofile.tutorial.store.interceptor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.microprofile.tutorial.store.demo.LoggingDemoService;

class LoggingInterceptorTest {

    @Test
    void testLoggingDemoService() {
        // This is a simple test to demonstrate how to use the logging interceptor
        // In a real scenario, you might use integration tests with Arquillian or similar
        
        LoggingDemoService service = new LoggingDemoService();
        
        // Call the logged method (in real tests, you'd verify the log output)
        String result = service.loggedMethod("test input");
        assertEquals("Processed: test input", result);
        
        // Call the non-logged method
        String result2 = service.nonLoggedMethod("other input");
        assertEquals("Silently processed: other input", result2);
        
        // Test exception handling
        Exception exception = assertThrows(Exception.class, () -> {
            service.methodWithException();
        });
        
        assertEquals("This exception will be logged by the interceptor", exception.getMessage());
    }
}
