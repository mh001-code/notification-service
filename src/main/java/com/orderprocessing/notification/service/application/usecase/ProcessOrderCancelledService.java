package com.orderprocessing.notification.service.application.usecase;

import com.orderprocessing.notification.service.application.port.in.ProcessOrderCancelledUseCase;
import com.orderprocessing.notification.service.application.port.out.NotificationRepository;
import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.Notification;
import com.orderprocessing.notification.service.domain.model.NotificationChannel;
import com.orderprocessing.notification.service.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessOrderCancelledService implements ProcessOrderCancelledUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationSenderPort notificationSender;

    @Override
    public void execute(UUID orderId, UUID customerId, String reason) {
        String message = "Your order #" + orderId + " has been cancelled. Reason: " + reason;
        Notification notification = Notification.create(
                customerId, orderId,
                NotificationType.ORDER_CANCELLATION,
                NotificationChannel.EMAIL,
                message
        );
        notificationRepository.save(notification);

        try {
            notificationSender.send(notification);
            notification.markSent();
        } catch (Exception e) {
            log.error("Failed to send ORDER_CANCELLATION for orderId={}", orderId, e);
            notification.markFailed();
        }
        notificationRepository.save(notification);
    }
}
