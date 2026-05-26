package com.orderprocessing.notification.service.application.usecase;

import com.orderprocessing.notification.service.application.port.out.NotificationRepository;
import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.Notification;
import com.orderprocessing.notification.service.domain.model.NotificationStatus;
import com.orderprocessing.notification.service.domain.model.NotificationType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessOrderCancelledServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSenderPort notificationSender;

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private ProcessOrderCancelledService service;

    private final UUID orderId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final String reason = "customer request";

    @BeforeEach
    void setUp() {
        service = new ProcessOrderCancelledService(notificationRepository, notificationSender, meterRegistry);
    }

    @Test
    void execute_shouldMarkNotificationAsSent_whenSenderSucceeds() {
        service.execute(orderId, customerId, reason);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        verify(notificationSender).send(any());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_CANCELLATION);
        assertThat(saved.getCustomerId()).isEqualTo(customerId);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    void execute_shouldMarkNotificationAsFailed_whenSenderThrows() {
        doThrow(new RuntimeException("send failed")).when(notificationSender).send(any());

        service.execute(orderId, customerId, reason);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getSentAt()).isNull();
    }
}
