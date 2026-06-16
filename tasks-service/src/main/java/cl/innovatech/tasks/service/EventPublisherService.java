package cl.innovatech.tasks.service;

import cl.innovatech.tasks.entity.OutboxEvent;
import cl.innovatech.tasks.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void publishToOutbox(String eventType, String payload) {
        OutboxEvent event = OutboxEvent.builder()
                .eventType(eventType)
                .payload(payload)
                .estado(OutboxEvent.EstadoEvento.PENDING)
                .build();
        outboxEventRepository.save(event);
        log.debug("Evento guardado en outbox: tipo={}", eventType);
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEventsToRedis() {
        List<OutboxEvent> pending = outboxEventRepository.findByEstado(OutboxEvent.EstadoEvento.PENDING);
        for (OutboxEvent event : pending) {
            try {
                redisTemplate.opsForStream().add("task-events", Map.of(
                        "eventType", event.getEventType(),
                        "payload", event.getPayload(),
                        "id", event.getId().toString()
                ));
                event.setEstado(OutboxEvent.EstadoEvento.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                log.info("Evento {} publicado a Redis Streams", event.getId());
            } catch (Exception ex) {
                log.error("Error publicando evento {} a Redis: {}", event.getId(), ex.getMessage());
                event.setEstado(OutboxEvent.EstadoEvento.FAILED);
            }
            outboxEventRepository.save(event);
        }
    }
}
