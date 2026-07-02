package com.example.reactive.resource;

import com.example.reactive.consumer.HelloConsumer;
import com.example.reactive.publisher.HelloPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for interacting with the Reactive Messaging system
 */
@ApplicationScoped
@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HelloResource {

    @Inject
    private HelloPublisher publisher;

    @Inject
    private HelloConsumer consumer;

    /**
     * Publish a custom message
     * POST /hello/publish
     * Body: { "message": "Your message here" }
     */
    @POST
    @Path("/publish")
    public Response publishMessage(Map<String, String> payload) {
        String message = payload.getOrDefault("message", "Hello from REST API");
        publisher.publishMessage(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "published");
        response.put("message", message);
        
        return Response.ok(response).build();
    }

    /**
     * Get all received messages
     * GET /hello/messages
     */
    @GET
    @Path("/messages")
    public Response getReceivedMessages() {
        List<String> messages = consumer.getReceivedMessages();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", messages.size());
        response.put("messages", messages);
        
        return Response.ok(response).build();
    }

    /**
     * Clear all received messages
     * DELETE /hello/messages
     */
    @DELETE
    @Path("/messages")
    public Response clearMessages() {
        consumer.clearMessages();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "cleared");
        
        return Response.ok(response).build();
    }

    /**
     * Health check endpoint
     * GET /hello/health
     */
    @GET
    @Path("/health")
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Reactive Messaging Demo");
        health.put("messagesReceived", consumer.getReceivedMessages().size());
        
        return Response.ok(health).build();
    }

    /**
     * Welcome endpoint
     * GET /hello
     */
    @GET
    public Response welcome() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Open Liberty Reactive Messaging Demo");
        info.put("endpoints", Map.of(
            "POST /hello/publish", "Publish a message",
            "GET /hello/messages", "Get received messages",
            "DELETE /hello/messages", "Clear received messages",
            "GET /hello/health", "Health check"
        ));
        
        return Response.ok(info).build();
    }
}
