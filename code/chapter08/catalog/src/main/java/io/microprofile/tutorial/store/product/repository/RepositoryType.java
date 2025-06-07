package io.microprofile.tutorial.store.product.repository;

import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CDI qualifier to distinguish between different repository implementations.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface RepositoryType {
    
    /**
     * Repository implementation type.
     */
    Type value();
    
    /**
     * Enumeration of repository types.
     */
    enum Type {
        JPA,
        IN_MEMORY
    }
}
