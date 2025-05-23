package io.microprofile.tutorial.store.product.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Readiness
@ApplicationScoped
public class ProductServiceReadinessCheck implements HealthCheck {

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
    
