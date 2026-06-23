package io.microprofile.tutorial.graphql.product.entity;

import org.eclipse.microprofile.graphql.Interface;
import org.eclipse.microprofile.graphql.Description;

/**
 * GraphQL interface for entities with unique identifiers.
 * Enables polymorphic queries across different entity types.
 */
@Interface
@Description("Common interface for entities with unique identifiers")
public interface Identifiable {
    
    @Description("Unique identifier for the entity")
    Long getId();
}
