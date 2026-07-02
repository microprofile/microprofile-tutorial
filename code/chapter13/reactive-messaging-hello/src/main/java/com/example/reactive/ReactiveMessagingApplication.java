package com.example.reactive;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration
 */
@ApplicationPath("/api")
public class ReactiveMessagingApplication extends Application {
    // No additional configuration needed
    // All JAX-RS resources will be automatically discovered
}
