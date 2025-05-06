package io.microprofile.tutorial.store.logging;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger; 

@Interceptor // Declare as an interceptor
public class LoggingInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class.getName());

    @AroundInvoke // Method to execute around the intercepted method
    public Object logMethodInvocation(InvocationContext ctx) throws Exception {
        LOGGER.info( "Entering method: " + ctx.getMethod().getName());

        Object result = ctx.proceed(); // Proceed to the original method

        LOGGER.info("Exiting method: " + ctx.getMethod().getName());
        return result;
    }
}
