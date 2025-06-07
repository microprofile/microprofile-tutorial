package com.example.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.enterprise.context.ApplicationScoped;

@Path("/hello")
@ApplicationScoped
public class HelloWorldResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello() {
        return "Hello, Open Liberty with GitHub Codespaces, JDK 21, MicroProfile 6.0, Jakarta EE 10!";
    }
}
