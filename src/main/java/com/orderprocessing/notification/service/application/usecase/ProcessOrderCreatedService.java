package com.orderprocessing.notification.service.application.usecase;

import com.orderprocessing.notification.service.application.port.in.ProcessOrderCreatedUseCase;
import com.orderprocessing.notification.service.application.port.out.NotificationRepository;
import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.Notification;
import com.orderprocessing.notification.service.domain.model.NotificationChannel;
import com.orderprocessing.notification.service.domain.model.NotificationType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessOrderCreatedService implements ProcessOrderCreatedUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationSenderPort notificationSender;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(UUID orderId, UUID customerId, BigDecimal totalAmount) {
        String message = "Your order #" + orderId + " has been confirmed. Total: R$" + totalAmount;
        Notification notification = Notification.create(
                customerId, orderId,
                NotificationType.ORDER_CONFIRMATION,
                NotificationChannel.EMAIL,
                message
        );
        notificationRepository.save(notification);

        try {
            notificationSender.send(notification);
            notification.markSent();
            Counter.builder("notifications.sent.total")
                    .tag("type", "ORDER_CONFIRMATION")
                    .tag("status", "success")
                    .description("Total de notificações enviadas")
                    .register(meterRegistry)
                    .increment();
        } catch (Exception e) {
            log.error("Failed to send ORDER_CONFIRMATION for orderId={}", orderId, e);
            notification.markFailed();
            Counter.builder("notifications.sent.total")
                    .tag("type", "ORDER_CONFIRMATION")
                    .tag("status", "failed")
                    .register(meterRegistry)
                    .increment();
        }
        notificationRepository.save(notification);
    }
}
