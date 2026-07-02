package io.microprofile.tutorial.store.order.service;

import io.microprofile.tutorial.store.order.entity.Order;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());

    public void notifyOrderCreated(Order order) {
        logger.info("Order created: orderId=" + order.getOrderId());
    }

    public void notifyOrderStatusChanged(Order order) {
        logger.info("Order status changed: orderId=" + order.getOrderId() + ", status=" + order.getStatus());
    }
}
