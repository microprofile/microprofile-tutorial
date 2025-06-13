package io.microprofile.tutorial.store.product.health;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness health check for the Product Catalog service.
 * Provides database connection health checks to monitor service health and availability.
 */
@Readiness
@ApplicationScoped
public class ProductServiceHealthCheck implements HealthCheck {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        if (isDatabaseConnectionHealthy()) {
            return HealthCheckResponse.named("ProductServiceReadinessCheck")
                    .up()
                    .build();
        } else {
            return HealthCheckResponse.named("ProductServiceReadinessCheck")
                    .down()
                    .build();
        }
    }

    private boolean isDatabaseConnectionHealthy(){
        try {
            // Perform a lightweight query to check the database connection
            entityManager.find(Product.class, 1L);
            return true;
        } catch (Exception e) {
            System.err.println("Database connection is not healthy: " + e.getMessage());
            return false;
        }
    }
}
