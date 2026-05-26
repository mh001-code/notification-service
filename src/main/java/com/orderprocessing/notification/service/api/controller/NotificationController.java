package com.orderprocessing.notification.service.api.controller;

import com.orderprocessing.notification.service.api.dto.NotificationResponse;
import com.orderprocessing.notification.service.application.port.out.NotificationRepository;
import com.orderprocessing.notification.service.domain.exception.NotificationNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifications", description = "Histórico de notificações enviadas aos clientes")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Operation(summary = "Listar notificações", description = "Filtra por customerId ou orderId. Retorna lista vazia se nenhum filtro informado.")
    @ApiResponse(responseCode = "200", description = "Lista de notificações (pode ser vazia)")
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

    @Operation(summary = "Buscar notificação por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificação encontrada"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        return notificationRepository.findById(id)
                .map(NotificationResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
