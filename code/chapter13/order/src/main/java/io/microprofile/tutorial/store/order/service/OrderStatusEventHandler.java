package io.microprofile.tutorial.store.order.service;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.microprofile.tutorial.store.order.entity.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.microprofile.tutorial.store.order.entity.Order;

@ApplicationScoped
public class OrderStatusEventHandler {

    @Inject
    OrderService orderService;

    @Inject
    NotificationService notificationService;

    @Incoming("payment-authorized")
    public void onPaymentAuthorized(Long orderId) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, OrderStatus.PAID);
        notificationService.notifyOrderStatusChanged(updatedOrder);
    }
}