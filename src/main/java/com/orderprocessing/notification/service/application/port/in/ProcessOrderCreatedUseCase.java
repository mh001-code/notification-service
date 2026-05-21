package com.orderprocessing.notification.service.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProcessOrderCreatedUseCase {
    void execute(UUID orderId, UUID customerId, BigDecimal totalAmount);
}
