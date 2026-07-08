package io.microprofile.tutorial.store.shoppingcart.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

import io.microprofile.tutorial.store.shoppingcart.repository.ShoppingCartRepository;

/**
 * Health checks for the Shopping Cart service.
 */
public class ShoppingCartHealthCheck {

    /**
     * Liveness check for the Shopping Cart service.
     * This check ensures that the application is running.
     */
    @Liveness
    @ApplicationScoped
    public static class LivenessCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("shopping-cart-service-liveness")
                    .up()
                    .withData("message", "Shopping Cart Service is alive")
                    .build();
        }
    }

    /**
     * Readiness check for the Shopping Cart service.
     * This check ensures that the application is ready to serve requests.
     */
    @Readiness
    @ApplicationScoped
    public static class ReadinessCheck implements HealthCheck {

        @Inject
        private ShoppingCartRepository cartRepository;

        @Override
        public HealthCheckResponse call() {
            boolean isReady;

            try {
                cartRepository.findAll();
                isReady = true;
            } catch (Exception e) {
                isReady = false;
            }

            return HealthCheckResponse.named("shopping-cart-service-readiness")
                    .status(isReady)
                    .withData("message", isReady
                            ? "Shopping Cart Service is ready"
                            : "Shopping Cart Service is not ready")
                    .build();
        }
    }
}
