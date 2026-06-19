package cl.innovatech.reports.consumer;

import cl.innovatech.reports.service.KpiCalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class TaskEventConsumerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private KpiCalculatorService kpiCalculatorService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StreamOperations streamOperations;

    @InjectMocks
    private TaskEventConsumer taskEventConsumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskEventConsumer, "streamName", "task-events");
        ReflectionTestUtils.setField(taskEventConsumer, "consumerGroup", "reports-service");
        ReflectionTestUtils.setField(taskEventConsumer, "consumerName", "reports-1");
    }

    @Test
    void consumeEvents_sinMensajes_noLlamaCalculator() {
        given(redisTemplate.opsForStream()).willReturn(streamOperations);
        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willReturn(List.of());

        taskEventConsumer.consumeEvents();

        verify(kpiCalculatorService, never()).recalculate(any());
    }

    @Test
    void consumeEvents_conMensajesNull_noLlamaCalculator() {
        given(redisTemplate.opsForStream()).willReturn(streamOperations);
        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willReturn(null);

        taskEventConsumer.consumeEvents();

        verify(kpiCalculatorService, never()).recalculate(any());
    }

    @Test
    void consumeEvents_conEventoTaskStatusChanged_llamaRecalculate() throws Exception {
        String payload = "{\"projectId\":7,\"taskId\":1}";
        MapRecord<String, Object, Object> message = StreamRecords.newRecord()
                .in("task-events")
                .ofMap(Map.of("eventType", "TASK_STATUS_CHANGED", "payload", payload));

        given(redisTemplate.opsForStream()).willReturn(streamOperations);
        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willReturn(List.of(message));
        given(objectMapper.readValue(eq(payload), eq(Map.class)))
                .willReturn(Map.of("projectId", 7));

        taskEventConsumer.consumeEvents();

        verify(kpiCalculatorService).recalculate(7L);
        verify(streamOperations).acknowledge(eq("task-events"), eq("reports-service"), any(RecordId.class));
    }

    @Test
    void consumeEvents_conEventoDesconocido_noLlamaCalculator() {
        MapRecord<String, Object, Object> message = StreamRecords.newRecord()
                .in("task-events")
                .ofMap(Map.of("eventType", "TASK_CREATED", "payload", "{}"));

        given(redisTemplate.opsForStream()).willReturn(streamOperations);
        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willReturn(List.of(message));

        taskEventConsumer.consumeEvents();

        verify(kpiCalculatorService, never()).recalculate(any());
        verify(streamOperations).acknowledge(eq("task-events"), eq("reports-service"), any(RecordId.class));
    }
}
