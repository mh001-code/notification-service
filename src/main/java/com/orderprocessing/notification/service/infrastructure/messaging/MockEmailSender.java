package com.orderprocessing.notification.service.infrastructure.messaging;

import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockEmailSender implements NotificationSenderPort {

    @Override
    public void send(Notification notification) {
        log.info("[EMAIL] customerId={} | type={} | message={}",
                notification.getCustomerId(), notification.getType(), notification.getMessage());
    }
}
