package io.microprofile.tutorial.store.product.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.ApplicationScoped;

@Liveness
@ApplicationScoped
public class ProductServiceLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Maximum amount of memory the JVM will attempt to use
        long allocatedMemory = runtime.totalMemory(); // Total memory currently allocated to the JVM
        long freeMemory = runtime.freeMemory(); // Amount of free memory within the allocated memory
        long usedMemory = allocatedMemory - freeMemory; // Actual memory used
        long availableMemory = maxMemory - usedMemory; // Total available memory

        long threshold = 50 * 1024 * 1024; // threshold: 100MB 

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("systemResourcesLiveness");

        if (availableMemory > threshold ) {
            // The system is considered live. Include data in the response for monitoring purposes.
            responseBuilder = responseBuilder.up()
                .withData("FreeMemory", freeMemory)
                .withData("MaxMemory", maxMemory)
                .withData("AllocatedMemory", allocatedMemory)
                .withData("UsedMemory", usedMemory)
                .withData("AvailableMemory", availableMemory);
        } else {
            // The system is not live. Include data in the response to aid in diagnostics.
            responseBuilder = responseBuilder.down()
                .withData("FreeMemory", freeMemory)
                .withData("MaxMemory", maxMemory)
                .withData("AllocatedMemory", allocatedMemory)
                .withData("UsedMemory", usedMemory)
                .withData("AvailableMemory", availableMemory);
        }

        return responseBuilder.build();
    }
}
