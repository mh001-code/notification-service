package com.orderprocessing.notification.service.api.dto;

import com.orderprocessing.notification.service.domain.model.Notification;
import com.orderprocessing.notification.service.domain.model.NotificationChannel;
import com.orderprocessing.notification.service.domain.model.NotificationStatus;
import com.orderprocessing.notification.service.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID customerId,
        UUID orderId,
        NotificationType type,
        NotificationChannel channel,
        String message,
        NotificationStatus status,
        Instant sentAt,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getCustomerId(), n.getOrderId(),
                n.getType(), n.getChannel(), n.getMessage(),
                n.getStatus(), n.getSentAt(), n.getCreatedAt()
        );
    }
}
