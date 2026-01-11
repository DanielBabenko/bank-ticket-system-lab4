package com.example.applicationservice.event;

import com.example.applicationservice.event.UserDeletedEvent;
import com.example.applicationservice.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UserDeletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(UserDeletedEventListener.class);
    private final ApplicationService applicationService;

    public UserDeletedEventListener(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.user-deleted:user.deleted}",
            groupId = "${spring.kafka.consumer.group-id:application-service-group}",
            containerFactory = "kafkaListenerContainerFactory" // Используем стандартный фабричный метод
    )
    public void handleUserDeleted(
            @Payload UserDeletedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📩 [Partition:{} Offset:{}] Received UserDeletedEvent for userId: {}", partition, offset, event.getUserId());

        try {
            // Вызываем сервисный метод. Предполагаем, что он возвращает Mono<Long>.
            Void deletedCount = applicationService.deleteApplicationsByUserId(event.getUserId()).block(); // Блокируем, т.к. внутри слушателя
            log.info("🗑️ Successfully deleted application(s) for userId: {}", event.getUserId());
        } catch (Exception e) {
            // Критическая ошибка. Сообщение НЕ будет подтверждено (ack),
            // и после retries будет отправлено в DLQ или логирование продолжится.
            log.error("❌ Failed to process UserDeletedEvent for userId: {}. Error: {}", event.getUserId(), e.getMessage());
            // Чтобы продолжить обработку других сообщений, можно выбросить исключение,
            // которое контейнер Kafka обработает согласно настройкам retry и error handler.
            throw new RuntimeException("Failed to delete applications for user: " + event.getUserId(), e);
        }
    }
}