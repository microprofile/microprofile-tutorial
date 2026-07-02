package com.example.reactive.publisher;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Message publisher that generates hello messages periodically
 */
@ApplicationScoped
public class HelloPublisher {
    
    private static final Logger LOGGER = Logger.getLogger(HelloPublisher.class.getName());
    private final AtomicInteger counter = new AtomicInteger(0);

    @Inject
    @Channel("hello-in")
    Emitter<String> helloEmitter;

    /**
     * Programmatic message publishing via Emitter
     */
    public void publishMessage(String message) {
        LOGGER.info("Publishing message via Emitter: " + message);
        helloEmitter.send(message);
    }

    /**
     * Stream-based publishing - generates messages every 10 seconds
     * Uncomment the @Outgoing annotation to enable automatic publishing
     */
    // @Outgoing("hello-in")
    public Publisher<String> generatePeriodicMessages() {
        return ReactiveStreams.generate(() -> {
            int count = counter.incrementAndGet();
            String message = "Hello from Open Liberty #" + count;
            LOGGER.info("Generated periodic message: " + message);
            return message;
        })
        .buildRs();
    }

    /**
     * Example message transformation helper.
     * Kept as a plain method so it does not require extra reactive channel wiring.
     */
    public String processMessage(String incoming) {
        String processed = incoming.toUpperCase() + " [PROCESSED]";
        LOGGER.info("Processing: " + incoming + " -> " + processed);
        return processed;
    }
}
