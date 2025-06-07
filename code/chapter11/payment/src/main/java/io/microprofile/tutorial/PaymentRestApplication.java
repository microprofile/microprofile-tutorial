package io.microprofile.tutorial;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class PaymentRestApplication extends Application {
    // No additional configuration is needed here
}