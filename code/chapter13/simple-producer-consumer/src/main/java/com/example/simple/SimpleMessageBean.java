package com.example.simple;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import java.util.logging.Logger;

@ApplicationScoped
public class SimpleMessageBean {

    private static final Logger LOGGER = Logger.getLogger(SimpleMessageBean.class.getName());

    @Outgoing("greetings-in")
    public PublisherBuilder<String> produce() {
        java.util.List<String> messages = java.util.List.of(
            "Hello, MicroProfile Reactive Messaging",
            "Reactive streams are non-blocking",
            "Backpressure is handled automatically"
        );
        messages.forEach(m -> LOGGER.info("Sending message: " + m));
        return ReactiveStreams.fromIterable(messages);
    }

    @Incoming("greetings-in")
    @Outgoing("greetings-out")
    public String process(String message) {
        LOGGER.info("Processing message: " + message);
        return message.toUpperCase();
    }

    @Incoming("greetings-out")
    public void consume(String message) {
        LOGGER.info("Received message: " + message);
    }
}
