package io.microprofile.tutorial.store.payment.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class PaymentServiceConfigSource implements ConfigSource{
    
    private Map<String, String> properties = new HashMap<>();

    public PaymentServiceConfigSource() {
        // Load payment service configurations dynamically
        // This example uses hardcoded values for demonstration
        properties.put("payment.gateway.apiKey", "secret_api_key");
        properties.put("payment.gateway.endpoint", "https://api.paymentgateway.com");
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return "PaymentServiceConfigSource";
    }

    @Override
    public int getOrdinal() {
        // Ensuring high priority to override default configurations if necessary
        return 600;
    }

    @Override
    public Set<String> getPropertyNames() {
        // Return the set of all property names available in this config source
        return properties.keySet();}
}
