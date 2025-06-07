package io.microprofile.tutorial.store.product.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Startup;

/**
 * Startup health check for the Product Catalog service.
 * Verifies that the EntityManagerFactory is properly initialized during application startup.
 */
@Startup
@ApplicationScoped
public class ProductServiceStartupCheck implements HealthCheck{

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Override
    public HealthCheckResponse call() {
        if (emf != null && emf.isOpen()) {
            return HealthCheckResponse.up("ProductServiceStartupCheck");
        } else {
            return HealthCheckResponse.down("ProductServiceStartupCheck");
        }
    }
}
