package com.orderprocessing.notification.service.api.controller;

import com.orderprocessing.notification.service.api.dto.NotificationResponse;
import com.orderprocessing.notification.service.application.port.out.NotificationRepository;
import com.orderprocessing.notification.service.domain.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID orderId) {

        List<NotificationResponse> result;

        if (customerId != null) {
            result = notificationRepository.findByCustomerId(customerId)
                    .stream().map(NotificationResponse::from).toList();
        } else if (orderId != null) {
            result = notificationRepository.findByOrderId(orderId)
                    .stream().map(NotificationResponse::from).toList();
        } else {
            result = List.of();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        return notificationRepository.findById(id)
                .map(NotificationResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
