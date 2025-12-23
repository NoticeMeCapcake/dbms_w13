package com.example.smartbulbs.config;

import com.example.smartbulbs.dto.LampCommand;
import com.example.smartbulbs.dto.LampTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // --- Producer Configuration ---
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ждать подтверждения от всех реплик
        props.put(ProducerConfig.RETRIES_CONFIG, 5); // 5 попыток повторной отправки
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Идемпотентность
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000); // Таймаут доставки 30 секунд

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // --- Consumer Configuration ---
    public <V>ConcurrentKafkaListenerContainerFactory<String, V> consumerFactory(String groupId, Class<V> valueClass) {
        ConcurrentKafkaListenerContainerFactory<String, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueClass);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        return factory;
    }

    // Фабрика для телеметрии
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LampTelemetry> telemetryConsumerFactory() {
        var baseFactory = consumerFactory("telemetry-processor", LampTelemetry.class);
        baseFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return baseFactory;
    }

    // Фабрика для команд
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LampCommand> commandConsumerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, LampCommand> baseFactory = consumerFactory("command-processor", LampCommand.class);

        // DLQ Sender
        baseFactory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
            log.error("Error processing command message: {}, Error: {}", record, exception.getMessage());
            LampCommand originalCommand = (LampCommand) record.value();
            String dlqMessage = String.format("{\"original_command\": %s, \"error_message\": \"%s\", \"timestamp\": \"%s\"}",
                    originalCommand != null ? originalCommand : "{}",
                    exception.getMessage(),
                    java.time.Instant.now().toString());

            kafkaTemplate.send("lamp-commands-dlq", (String) record.key(), dlqMessage);
            System.out.println("Sent failed command to DLQ: " + dlqMessage);
        }, new FixedBackOff(1000, 3)));

        baseFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return baseFactory;
    }


    // --- Topic Configuration ---
    @Bean
    public NewTopic lampTelemetryTopic() {
        return TopicBuilder.name("lamp-telemetry")
                .partitions(3)
                .replicas(3)
                .config("retention.ms", String.valueOf(24 * 60 * 60 * 1000L)) // 24 часа
                .build();
    }

    @Bean
    public NewTopic lampCommandsTopic() {
        return TopicBuilder.name("lamp-commands")
                .partitions(2)
                .replicas(3)
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7 дней
                .build();
    }

    @Bean
    public NewTopic lampAnalyticsTopic() {
        return TopicBuilder.name("lamp-analytics")
                .partitions(1)
                .replicas(3)
                .config("retention.ms", String.valueOf(30 * 24 * 60 * 60 * 1000L)) // 30 дней
                .build();
    }

    @Bean
    public NewTopic lampCommandsDLQTopic() {
        return TopicBuilder.name("lamp-commands-dlq")
                .partitions(1)
                .replicas(3)
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L)) // 7 дней
                .build();
    }
}