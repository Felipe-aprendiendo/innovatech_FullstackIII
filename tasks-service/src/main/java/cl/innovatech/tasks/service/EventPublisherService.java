package cl.innovatech.tasks.service;

import cl.innovatech.tasks.entity.OutboxEvent;
import cl.innovatech.tasks.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final OutboxEventRepository outboxEventRepository;

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

    // TODO: Activar cuando Redis esté disponible
    // @Scheduled(fixedDelay = 5000)
    // @Transactional
    // public void publishPendingEventsToRedis() {
    //     List<OutboxEvent> pending = outboxEventRepository.findByEstado(OutboxEvent.EstadoEvento.PENDING);
    //     for (OutboxEvent event : pending) {
    //         try {
    //             redisTemplate.opsForStream().add("task-events", Map.of(
    //                 "eventType", event.getEventType(),
    //                 "payload", event.getPayload()
    //             ));
    //             event.setEstado(OutboxEvent.EstadoEvento.PUBLISHED);
    //             event.setPublishedAt(LocalDateTime.now());
    //         } catch (Exception ex) {
    //             log.error("Error publicando evento {} a Redis: {}", event.getId(), ex.getMessage());
    //             event.setEstado(OutboxEvent.EstadoEvento.FAILED);
    //         }
    //         outboxEventRepository.save(event);
    //     }
    // }
}
