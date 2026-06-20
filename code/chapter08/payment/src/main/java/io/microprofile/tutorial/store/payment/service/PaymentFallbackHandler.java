package io.microprofile.tutorial.store.payment.service;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PaymentFallbackHandler implements FallbackHandler<CompletionStage<String>> {

    @Override
    public CompletionStage<String> handle(ExecutionContext context) {
        if (context.getFailure() instanceof BulkheadException) {
            System.out.println("Bulkhead rejected request for method: " + context.getMethod().getName());
            return CompletableFuture.completedFuture(
                "{\"status\":\"rejected\", \"message\":\"Bulkhead full: too many concurrent requests.\"}");
        }
        System.out.println("Fallback invoked: " + context.getFailure().getMessage());
        return CompletableFuture.completedFuture(
            "{\"status\":\"failed\", \"message\":\"Payment service is currently unavailable.\"}");
    }
}
