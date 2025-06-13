package io.microprofile.tutorial.store.user;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Application class to activate REST resources.
 */
@ApplicationPath("/api")
public class UserApplication extends Application {
    // The resources will be automatically discovered
}
