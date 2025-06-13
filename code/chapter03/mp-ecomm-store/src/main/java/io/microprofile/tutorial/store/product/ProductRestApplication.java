package io.microprofile.tutorial.store.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.Initialized;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@ApplicationPath("/api")
public class ProductRestApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(ProductRestApplication.class.getName());
    
    /**
     * Initializes the application and loads custom logging configuration
     */
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        try {
            // Load logging configuration
            InputStream inputStream = ProductRestApplication.class
                .getClassLoader()
                .getResourceAsStream("logging.properties");
                
            if (inputStream != null) {
                LogManager.getLogManager().readConfiguration(inputStream);
                LOGGER.info("Custom logging configuration loaded");
            } else {
                LOGGER.warning("Could not find logging.properties file");
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to load logging configuration: " + e.getMessage());
        }
    }
}