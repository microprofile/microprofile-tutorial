package com.example.reactive.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Message consumer that receives and processes hello messages
 */
@ApplicationScoped
public class HelloConsumer {
    
    private static final Logger LOGGER = Logger.getLogger(HelloConsumer.class.getName());
    
    // Thread-safe list to store received messages
    private final CopyOnWriteArrayList<String> receivedMessages = new CopyOnWriteArrayList<>();

    /**
     * Simple message consumption - payload only
     */
    @Incoming("hello-out")
    public void consumeHelloMessage(String message) {
        LOGGER.info("✓ Received message: " + message);
        receivedMessages.add(message);
        
        // Simulate some processing
        processMessage(message);
    }

    /**
     * Advanced message consumption with acknowledgment
     * Uncomment to use this instead of the simple consumer above
     */
    /*
    @Incoming("hello-out")
    public CompletionStage<Void> consumeHelloMessageWithAck(Message<String> message) {
        String payload = message.getPayload();
        LOGGER.info("✓ Received message with ack: " + payload);
        receivedMessages.add(payload);
        
        processMessage(payload);
        
        // Acknowledge the message
        return message.ack();
    }
    */

    private void processMessage(String message) {
        // Add your business logic here
        LOGGER.info("Processing: " + message);
        
        if (message.contains("error")) {
            LOGGER.warning("Message contains error keyword!");
        }
    }

    /**
     * Get all received messages (for testing/monitoring)
     */
    public CopyOnWriteArrayList<String> getReceivedMessages() {
        return receivedMessages;
    }

    /**
     * Clear received messages
     */
    public void clearMessages() {
        receivedMessages.clear();
        LOGGER.info("Cleared all received messages");
    }
}
