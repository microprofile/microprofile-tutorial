package com.example.reactive.model;

import java.time.LocalDateTime;

/**
 * Simple message model for demonstration
 */
public class HelloMessage {
    private String content;
    private LocalDateTime timestamp;
    private String sender;

    public HelloMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public HelloMessage(String content, String sender) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "HelloMessage{" +
                "content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", sender='" + sender + '\'' +
                '}';
    }
}
