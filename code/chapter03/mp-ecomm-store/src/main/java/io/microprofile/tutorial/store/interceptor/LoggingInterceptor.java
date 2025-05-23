package io.microprofile.tutorial.store.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging interceptor that logs method entry, exit, and execution time.
 * This interceptor is applied to methods or classes annotated with @Logged.
 */
@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {
    
    @AroundInvoke
    public Object logMethodCall(InvocationContext context) throws Exception {
        final String className = context.getTarget().getClass().getName();
        final String methodName = context.getMethod().getName();
        final Logger logger = Logger.getLogger(className);
        
        // Log method entry with parameters
        logger.log(Level.INFO, "Entering method: {0}.{1} with parameters: {2}",
                new Object[]{className, methodName, Arrays.toString(context.getParameters())});
        
        final long startTime = System.currentTimeMillis();
        
        try {
            // Execute the intercepted method
            Object result = context.proceed();
            
            final long executionTime = System.currentTimeMillis() - startTime;
            
            // Log method exit with execution time
            logger.log(Level.INFO, "Exiting method: {0}.{1}, execution time: {2}ms, result: {3}",
                    new Object[]{className, methodName, executionTime, result});
            
            return result;
        } catch (Exception e) {
            // Log exceptions
            final long executionTime = System.currentTimeMillis() - startTime;
            logger.log(Level.SEVERE, "Exception in method: {0}.{1}, execution time: {2}ms, exception: {3}",
                    new Object[]{className, methodName, executionTime, e.getMessage()});
            
            // Re-throw the exception
            throw e;
        }
    }
}
