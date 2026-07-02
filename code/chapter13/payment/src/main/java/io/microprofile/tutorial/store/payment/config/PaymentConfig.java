package io.microprofile.tutorial.store.payment.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Utility class for accessing payment service configuration.
 */
public class PaymentConfig {

    private static final Config config = ConfigProvider.getConfig();
    
    /**
     * Gets a configuration property as a String.
     * 
     * @param key the property key
     * @return the property value
     */
    public static String getConfigProperty(String key) {
        return config.getValue(key, String.class);
    }
    
    /**
     * Gets a configuration property as a String with a default value.
     * 
     * @param key the property key
     * @param defaultValue the default value if the key doesn't exist
     * @return the property value or the default value
     */
    public static String getConfigProperty(String key, String defaultValue) {
        return config.getOptionalValue(key, String.class).orElse(defaultValue);
    }
    
    /**
     * Gets a configuration property as an Integer.
     * 
     * @param key the property key
     * @return the property value as an Integer
     */
    public static Integer getIntProperty(String key) {
        return config.getValue(key, Integer.class);
    }
    
    /**
     * Gets a configuration property as a Boolean.
     * 
     * @param key the property key
     * @return the property value as a Boolean
     */
    public static Boolean getBooleanProperty(String key) {
        return config.getValue(key, Boolean.class);
    }
    
    /**
     * Updates a configuration property at runtime through the custom ConfigSource.
     * 
     * @param key the property key
     * @param value the property value
     */
    public static void updateProperty(String key, String value) {
        PaymentServiceConfigSource.setProperty(key, value);
    }
}
