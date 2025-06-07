package io.microprofile.tutorial.store.payment.config;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Custom ConfigSource for Payment Service.
 * This config source provides payment-specific configuration with high priority.
 */
public class PaymentServiceConfigSource implements ConfigSource {

    private static final Map<String, String> properties = new HashMap<>();

    private static final String NAME = "PaymentServiceConfigSource";
    private static final int ORDINAL = 600; // Higher ordinal means higher priority
    
    public PaymentServiceConfigSource() {
       // Load payment service configurations dynamically
       // This example uses hardcoded values for demonstration
       properties.put("payment.gateway.endpoint", "https://api.paymentgateway.com");
   }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }
    
    /**
     * Updates a configuration property at runtime.
     * 
     * @param key the property key
     * @param value the property value
     */
    public static void setProperty(String key, String value) {
        properties.put(key, value);
    }
}
