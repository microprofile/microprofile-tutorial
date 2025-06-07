package io.microprofile.tutorial.store.shoppingcart;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * REST application for shopping cart management.
 */
@ApplicationPath("/api")
public class ShoppingCartApplication extends Application {
    // The resources will be discovered automatically
}
