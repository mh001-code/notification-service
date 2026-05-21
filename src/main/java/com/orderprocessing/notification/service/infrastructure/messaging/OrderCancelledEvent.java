package com.orderprocessing.notification.service.infrastructure.messaging;

import java.util.UUID;

public record OrderCancelledEvent(UUID orderId, UUID customerId, String reason) {}
