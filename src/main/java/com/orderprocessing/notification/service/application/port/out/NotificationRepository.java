package com.orderprocessing.notification.service.application.port.out;

import com.orderprocessing.notification.service.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    List<Notification> findByCustomerId(UUID customerId);
    List<Notification> findByOrderId(UUID orderId);
}
