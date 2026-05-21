package com.orderprocessing.notification.service.infrastructure.messaging;

import com.orderprocessing.notification.service.application.port.in.ProcessOrderCancelledUseCase;
import com.orderprocessing.notification.service.application.port.in.ProcessOrderCreatedUseCase;
import com.orderprocessing.notification.service.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ProcessOrderCreatedUseCase processOrderCreated;
    private final ProcessOrderCancelledUseCase processOrderCancelled;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created: orderId={}", event.orderId());
        try {
            processOrderCreated.execute(event.orderId(), event.customerId(), event.totalAmount());
            log.info("Notification processed for orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process order.created: orderId={}, error={}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order.cancelled: orderId={}", event.orderId());
        try {
            processOrderCancelled.execute(event.orderId(), event.customerId(), event.reason());
            log.info("Notification processed for orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process order.cancelled: orderId={}, error={}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }
}
