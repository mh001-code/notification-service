package com.orderprocessing.notification.service.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
public class Notification {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private Instant sentAt;

    @Column(nullable = false)
    private Instant createdAt;

    public static Notification create(UUID customerId, UUID orderId,
                                      NotificationType type, NotificationChannel channel,
                                      String message) {
        Notification n = new Notification();
        n.id = UUID.randomUUID();
        n.customerId = customerId;
        n.orderId = orderId;
        n.type = type;
        n.channel = channel;
        n.message = message;
        n.status = NotificationStatus.PENDING;
        n.createdAt = Instant.now();
        return n;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }
}
