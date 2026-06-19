package cl.innovatech.tasks.service;

import cl.innovatech.tasks.entity.OutboxEvent;
import cl.innovatech.tasks.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class EventPublisherServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations streamOperations;

    @InjectMocks
    private EventPublisherService eventPublisherService;

    @Test
    void publishToOutbox_guardaEventoEnEstadoPending() {
        given(outboxEventRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        eventPublisherService.publishToOutbox("TASK_STATUS_CHANGED", "{\"taskId\":1}");

        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void publishPendingEventsToRedis_publicaEventoYCambiaEstadoAPublished() {
        OutboxEvent event = OutboxEvent.builder()
                .id(1L).eventType("TASK_STATUS_CHANGED").payload("{\"taskId\":1}")
                .estado(OutboxEvent.EstadoEvento.PENDING).build();

        given(outboxEventRepository.findByEstado(OutboxEvent.EstadoEvento.PENDING))
                .willReturn(List.of(event));
        given(redisTemplate.opsForStream()).willReturn(streamOperations);
        given(streamOperations.add(any(), any(Map.class))).willReturn(null);

        eventPublisherService.publishPendingEventsToRedis();

        verify(streamOperations).add(eq("task-events"), any(Map.class));
        verify(outboxEventRepository).save(event);
    }

    @Test
    void publishPendingEventsToRedis_errorDeRedis_marcaComoFailed() {
        OutboxEvent event = OutboxEvent.builder()
                .id(1L).eventType("TASK_STATUS_CHANGED").payload("{\"taskId\":1}")
                .estado(OutboxEvent.EstadoEvento.PENDING).build();

        given(outboxEventRepository.findByEstado(OutboxEvent.EstadoEvento.PENDING))
                .willReturn(List.of(event));
        given(redisTemplate.opsForStream()).willReturn(streamOperations);
        given(streamOperations.add(any(), any(Map.class))).willThrow(new RuntimeException("Redis caído"));

        eventPublisherService.publishPendingEventsToRedis();

        verify(outboxEventRepository).save(event);
    }

    @Test
    void publishPendingEventsToRedis_sinEventosPendientes_noPublicaNada() {
        given(outboxEventRepository.findByEstado(OutboxEvent.EstadoEvento.PENDING))
                .willReturn(List.of());

        eventPublisherService.publishPendingEventsToRedis();

        verify(redisTemplate, never()).opsForStream();
    }
}
