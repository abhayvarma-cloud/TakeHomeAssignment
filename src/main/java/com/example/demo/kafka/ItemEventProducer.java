package com.example.demo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ItemEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ItemEventProducer.class);
    private static final String TOPIC = "item-events";

    private final KafkaOperations<String, String> kafkaOperations;

    public ItemEventProducer(KafkaOperations<String, String> kafkaOperations) {
        this.kafkaOperations = kafkaOperations;
    }

    public void publishCreated(Long itemId, String itemName) {
        String message = String.format("{\"event\":\"CREATED\",\"id\":%d,\"name\":\"%s\"}", itemId, itemName);
        send(String.valueOf(itemId), message);
    }

    public void publishDeleted(Long itemId) {
        String message = String.format("{\"event\":\"DELETED\",\"id\":%d}", itemId);
        send(String.valueOf(itemId), message);
    }

    private void send(String key, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaOperations.send(TOPIC, key, message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to {}: {}", TOPIC, ex.getMessage());
            } else {
                log.info("Published to {} partition={} offset={}: {}",
                        TOPIC,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        message);
            }
        });
    }
}
