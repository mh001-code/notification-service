package com.orderprocessing.notification.service.infrastructure.messaging;

import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockEmailSender implements NotificationSenderPort {

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void send(Notification notification) {
        if (Math.random() < 0.1) {
            log.warn("[EMAIL] Delivery failed (simulated), will retry. customerId={}", notification.getCustomerId());
            throw new RuntimeException("Simulated email delivery failure");
        }
        log.info("[EMAIL] customerId={} | type={} | message={}",
                notification.getCustomerId(), notification.getType(), notification.getMessage());
    }

    @Recover
    public void recover(RuntimeException e, Notification notification) {
        log.error("[EMAIL] All retries exhausted for customerId={} | type={}",
                notification.getCustomerId(), notification.getType());
        throw e;
    }
}
