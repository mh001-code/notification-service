package com.orderprocessing.notification.service.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Serviço de notificações. Consome eventos do RabbitMQ e envia " +
                                "emails de confirmação/cancelamento de pedidos. Retry automático com " +
                                "backoff de 1s e até 3 tentativas via Spring Retry.")
                        .version("1.0.0")
                        .contact(new Contact().name("Order Processing System")))
                .servers(List.of(new Server().url("http://localhost:8082").description("Local")));
    }
}
