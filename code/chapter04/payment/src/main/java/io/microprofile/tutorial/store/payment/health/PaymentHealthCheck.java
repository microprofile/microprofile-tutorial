package io.microprofile.tutorial.store.payment.health;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health checks for the Payment service.
 */
@ApplicationScoped
public class PaymentHealthCheck {

    /**
     * Liveness check for the Payment service.
     * This check ensures that the application is running.
     *
     * @return A HealthCheckResponse indicating whether the service is alive
     */
    @Liveness
    public HealthCheck paymentLivenessCheck() {
        return () -> HealthCheckResponse.named("payment-service-liveness")
                .up()
                .withData("message", "Payment Service is alive")
                .build();
    }

    /**
     * Readiness check for the Payment service.
     * This check ensures that the application is ready to serve requests.
     * In a real application, this would check dependencies like databases.
     *
     * @return A HealthCheckResponse indicating whether the service is ready
     */
    @Readiness
    public HealthCheck paymentReadinessCheck() {
        return () -> HealthCheckResponse.named("payment-service-readiness")
                .up()
                .withData("message", "Payment Service is ready")
                .build();
    }
}
