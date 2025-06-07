// package io.microprofile.tutorial.store.shoppingcart.health;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject;

// import org.eclipse.microprofile.health.HealthCheck;
// import org.eclipse.microprofile.health.HealthCheckResponse;
// import org.eclipse.microprofile.health.Liveness;
// import org.eclipse.microprofile.health.Readiness;

// import io.microprofile.tutorial.store.shoppingcart.repository.ShoppingCartRepository;

// /**
//  * Health checks for the Shopping Cart service.
//  */
// @ApplicationScoped
// public class ShoppingCartHealthCheck {

//     @Inject
//     private ShoppingCartRepository cartRepository;

//     /**
//      * Liveness check for the Shopping Cart service.
//      * This check ensures that the application is running.
//      *
//      * @return A HealthCheckResponse indicating whether the service is alive
//      */
//     @Liveness
//     public HealthCheck shoppingCartLivenessCheck() {
//         return () -> HealthCheckResponse.named("shopping-cart-service-liveness")
//                 .up()
//                 .withData("message", "Shopping Cart Service is alive")
//                 .build();
//     }

//     /**
//      * Readiness check for the Shopping Cart service.
//      * This check ensures that the application is ready to serve requests.
//      * In a real application, this would check dependencies like databases.
//      *
//      * @return A HealthCheckResponse indicating whether the service is ready
//      */
//     @Readiness
//     public HealthCheck shoppingCartReadinessCheck() {
//         boolean isReady = true;
        
//         try {
//             // Simple check to ensure repository is functioning
//             cartRepository.findAll();
//         } catch (Exception e) {
//             isReady = false;
//         }
        
//         return () -> {
//             if (isReady) {
//                 return HealthCheckResponse.named("shopping-cart-service-readiness")
//                     .up()
//                     .withData("message", "Shopping Cart Service is ready")
//                     .build();
//             } else {
//                 return HealthCheckResponse.named("shopping-cart-service-readiness")
//                     .down()
//                     .withData("message", "Shopping Cart Service is not ready")
//                     .build();
//             }
//         };
//     }
// }
