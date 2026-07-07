package io.microprofile.tutorial.store.payment.service;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * Fallback handler for {@link PaymentService#sendPaymentNotification}.
 *
 * Unlike a {@code fallbackMethod}, a {@link FallbackHandler} receives the
 * {@link ExecutionContext}, which exposes the triggering {@link Throwable} via
 * {@link ExecutionContext#getFailure()}. That lets the fallback report which
 * fault tolerance pattern actually intervened: Bulkhead (capacity exceeded),
 * Timeout (execution took too long), or the simulated gateway failure.
 */
public class NotificationFallbackHandler implements FallbackHandler<CompletionStage<String>> {

    private static final Logger logger = Logger.getLogger(NotificationFallbackHandler.class.getName());

    @Override
    public CompletionStage<String> handle(ExecutionContext context) {
        String paymentId = (String) context.getParameters()[0];
        String reason = describeFailure(context.getFailure());

        logger.warning("Failed to send notification for payment: " + paymentId + " (" + reason + ")");

        // In production: queue notification for retry, use backup channel (SMS if email failed), etc.
        return CompletableFuture.completedFuture("Notification queued for retry: " + reason);
    }

    private String describeFailure(Throwable cause) {
        if (cause instanceof BulkheadException) {
            return "bulkhead capacity exceeded";
        } else if (cause instanceof TimeoutException) {
            return "request timed out";
        } else {
            return "notification service unavailable";
        }
    }
}
