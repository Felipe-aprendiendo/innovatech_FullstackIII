package cl.innovatech.reports.consumer;

import cl.innovatech.reports.service.KpiCalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final KpiCalculatorService kpiCalculatorService;
    private final ObjectMapper objectMapper;

    @Value("${app.redis.stream}")
    private String streamName;

    @Value("${app.redis.consumer-group}")
    private String consumerGroup;

    @Value("${app.redis.consumer-name}")
    private String consumerName;

    @PostConstruct
    public void initConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0"), consumerGroup);
            log.info("Consumer group '{}' creado en stream '{}'", consumerGroup, streamName);
        } catch (Exception ex) {
            // El grupo ya existe o el stream no existe aún — ambos casos son normales al arrancar
            log.debug("Consumer group ya existe o stream no disponible: {}", ex.getMessage());
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void consumeEvents() {
        try {
            List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
                    .read(Consumer.from(consumerGroup, consumerName),
                            StreamReadOptions.empty().count(10).block(Duration.ofMillis(500)),
                            StreamOffset.create(streamName, ReadOffset.lastConsumed()));

            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (MapRecord<String, Object, Object> message : messages) {
                try {
                    processMessage(message);
                    redisTemplate.opsForStream().acknowledge(streamName, consumerGroup, message.getId());
                } catch (Exception ex) {
                    log.error("Error procesando mensaje {}: {}", message.getId(), ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.debug("Redis no disponible para consumir eventos: {}", ex.getMessage());
        }
    }

    private void processMessage(MapRecord<String, Object, Object> message) {
        Map<Object, Object> body = message.getValue();
        String eventType = (String) body.get("eventType");
        String payload = (String) body.get("payload");

        log.debug("Evento recibido: type={}, id={}", eventType, message.getId());

        if ("TASK_STATUS_CHANGED".equals(eventType) && payload != null) {
            try {
                Map<?, ?> data = objectMapper.readValue(payload, Map.class);
                Object projectIdObj = data.get("projectId");
                if (projectIdObj != null) {
                    Long projectId = ((Number) projectIdObj).longValue();
                    kpiCalculatorService.recalculate(projectId);
                }
            } catch (Exception ex) {
                log.error("Error parseando payload del evento: {}", ex.getMessage());
            }
        }
    }
}
