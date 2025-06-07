package io.microprofile.tutorial.store.shipment.health;

import io.microprofile.tutorial.store.shipment.client.OrderClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health check for the shipment service.
 */
@ApplicationScoped
public class ShipmentHealthCheck {

    @Inject
    private OrderClient orderClient;

    /**
     * Liveness check for the shipment service.
     * Verifies that the application is running and not in a failed state.
     *
     * @return HealthCheckResponse indicating whether the service is live
     */
    @Liveness
    @ApplicationScoped
    public static class LivenessCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("shipment-liveness")
                    .up()
                    .withData("memory", Runtime.getRuntime().freeMemory())
                    .build();
        }
    }

    /**
     * Readiness check for the shipment service.
     * Verifies that the service is ready to handle requests, including connectivity to dependencies.
     *
     * @return HealthCheckResponse indicating whether the service is ready
     */
    @Readiness
    @ApplicationScoped
    public class ReadinessCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            boolean orderServiceReachable = false;
            
            try {
                // Simple check to see if the Order service is reachable
                // We use a dummy order ID just to test connectivity
                orderClient.getShippingAddress(999999L);
                orderServiceReachable = true;
            } catch (Exception e) {
                // If the order service is not reachable, the health check will fail
                orderServiceReachable = false;
            }
            
            return HealthCheckResponse.named("shipment-readiness")
                    .status(orderServiceReachable)
                    .withData("orderServiceReachable", orderServiceReachable)
                    .build();
        }
    }
}
