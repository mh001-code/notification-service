package com.orderprocessing.notification.service.infrastructure.persistence;

import com.orderprocessing.notification.service.application.port.out.NotificationRepository;
import com.orderprocessing.notification.service.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Notification> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Notification> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}
