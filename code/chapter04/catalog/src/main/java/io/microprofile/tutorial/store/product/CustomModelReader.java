package io.microprofile.tutorial.store.product;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * Custom OpenAPI model reader demonstrating jsonSchemaDialect support.
 * This reader is called once during application startup to build/enhance the OpenAPI model.
 */
public class CustomModelReader implements OASModelReader {
    
    @Override
    public OpenAPI buildModel() {
        // Create an OpenAPI object with jsonSchemaDialect
        return OASFactory.createOpenAPI()
            .openapi("3.1.0")
            .jsonSchemaDialect("https://spec.openapis.org/oas/3.1/dialect/base")
            .info(OASFactory.createInfo()
                .title("Product API")
                .version("1.0.0")
                .description("API for managing products with MicroProfile OpenAPI 4.1"));
    }
}
