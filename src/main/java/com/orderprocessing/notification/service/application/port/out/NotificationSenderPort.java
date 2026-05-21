package com.orderprocessing.notification.service.application.port.out;

import com.orderprocessing.notification.service.domain.model.Notification;

public interface NotificationSenderPort {
    void send(Notification notification);
}
