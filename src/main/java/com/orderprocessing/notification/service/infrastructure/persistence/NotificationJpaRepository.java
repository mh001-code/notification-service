package com.orderprocessing.notification.service.infrastructure.persistence;

import com.orderprocessing.notification.service.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByCustomerId(UUID customerId);
    List<Notification> findByOrderId(UUID orderId);
}
