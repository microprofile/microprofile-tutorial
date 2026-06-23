package io.microprofile.tutorial.graphql.product.api;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * GraphQL API demonstrating dynamic configuration
 */
@GraphQLApi
@ApplicationScoped
@Description("Dynamic configuration API")
public class ConfigurableApi {
    
    @Query
    @Description("Retrieves a configuration value by key at runtime")
    public String getConfigValue(@Name("key") String key) {
        Config config = ConfigProvider.getConfig();
        return config.getOptionalValue(key, String.class)
                     .orElse("Not configured");
    }
}
