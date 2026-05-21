package com.orderprocessing.notification.service.application.port.in;

import java.util.UUID;

public interface ProcessOrderCancelledUseCase {
    void execute(UUID orderId, UUID customerId, String reason);
}
