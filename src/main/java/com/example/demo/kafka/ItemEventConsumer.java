package com.example.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class ItemEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ItemEventConsumer.class);

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "item-events", groupId = "demo-group")
    public void consume(ConsumerRecord<String, String> record) {
        log.info("Consumed item event — key={} partition={} offset={} value={}",
                record.key(),
                record.partition(),
                record.offset(),
                record.value());
    }
}
