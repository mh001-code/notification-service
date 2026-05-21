package com.orderprocessing.notification.service;

import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.NotificationStatus;
import com.orderprocessing.notification.service.domain.model.NotificationType;
import com.orderprocessing.notification.service.infrastructure.config.RabbitMQConfig;
import com.orderprocessing.notification.service.infrastructure.messaging.OrderCancelledEvent;
import com.orderprocessing.notification.service.infrastructure.messaging.OrderCreatedEvent;
import com.orderprocessing.notification.service.infrastructure.persistence.NotificationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class OrderEventConsumerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationJpaRepository notificationRepository;

    @MockBean
    private NotificationSenderPort notificationSender;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void onOrderCreated_shouldSaveNotificationAsSent_whenSenderSucceeds() {
        UUID orderId = UUID.randomUUID();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_CREATED_KEY,
                new OrderCreatedEvent(orderId, UUID.randomUUID(), List.of(), BigDecimal.valueOf(150)));

        await().atMost(5, SECONDS).untilAsserted(() -> {
            var notifications = notificationRepository.findByOrderId(orderId);
            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.ORDER_CONFIRMATION);
        });
    }

    @Test
    void onOrderCreated_shouldSaveNotificationAsFailed_whenSenderThrows() {
        doThrow(new RuntimeException("send failed")).when(notificationSender).send(any());
        UUID orderId = UUID.randomUUID();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_CREATED_KEY,
                new OrderCreatedEvent(orderId, UUID.randomUUID(), List.of(), BigDecimal.valueOf(50)));

        await().atMost(5, SECONDS).untilAsserted(() -> {
            var notifications = notificationRepository.findByOrderId(orderId);
            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
        });
    }

    @Test
    void onOrderCancelled_shouldSaveNotificationAsSent_whenSenderSucceeds() {
        UUID orderId = UUID.randomUUID();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_CANCELLED_KEY,
                new OrderCancelledEvent(orderId, UUID.randomUUID(), "customer request"));

        await().atMost(5, SECONDS).untilAsserted(() -> {
            var notifications = notificationRepository.findByOrderId(orderId);
            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.ORDER_CANCELLATION);
        });
    }
}
