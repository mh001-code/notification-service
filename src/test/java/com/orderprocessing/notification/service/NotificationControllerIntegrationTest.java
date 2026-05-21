package com.orderprocessing.notification.service;

import com.orderprocessing.notification.service.application.port.out.NotificationSenderPort;
import com.orderprocessing.notification.service.domain.model.Notification;
import com.orderprocessing.notification.service.domain.model.NotificationChannel;
import com.orderprocessing.notification.service.domain.model.NotificationType;
import com.orderprocessing.notification.service.infrastructure.persistence.NotificationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NotificationJpaRepository notificationRepository;

    @MockBean
    private NotificationSenderPort notificationSender;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void getByCustomerId_shouldReturn200_withMatchingNotifications() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        notificationRepository.save(Notification.create(
                customerId, orderId, NotificationType.ORDER_CONFIRMATION, NotificationChannel.EMAIL, "test"));

        ResponseEntity<List> response = restTemplate.getForEntity(
                "/notifications?customerId=" + customerId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getByOrderId_shouldReturn200_withMatchingNotifications() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        notificationRepository.save(Notification.create(
                customerId, orderId, NotificationType.ORDER_CANCELLATION, NotificationChannel.EMAIL, "test"));

        ResponseEntity<List> response = restTemplate.getForEntity(
                "/notifications?orderId=" + orderId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getById_shouldReturn200_whenFound() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Notification saved = notificationRepository.save(Notification.create(
                customerId, orderId, NotificationType.ORDER_CONFIRMATION, NotificationChannel.EMAIL, "test"));

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/notifications/" + saved.getId(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("id")).isEqualTo(saved.getId().toString());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/notifications/" + UUID.randomUUID(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
