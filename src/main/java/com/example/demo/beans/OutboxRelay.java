package com.example.demo.beans;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 200;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;

    public OutboxRelay(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka) {
        this.jdbc = jdbc;
        this.kafka = kafka;
    }

    private record Row(UUID id, String topic, String key, String eventType, String payload) {}

    @Scheduled(fixedDelay = 300)
    @Transactional
    public void publish() throws Exception {

        // 1. Claim pending rows. SKIP LOCKED lets several app instances
        //    run the relay at once without taking the same rows.
        List<Row> rows = jdbc.query("""
                select id, topic, aggregate_id, event_type, payload::text
                  from outbox_event
                 where status = 'PENDING'
                 order by created_at
                 limit ?
                 for update skip locked
                """,
                (rs, i) -> new Row(
                        rs.getObject(1, UUID.class),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5)),
                BATCH_SIZE);

        if (rows.isEmpty()) {
            return;
        }

        // 2. Send every row to its own topic, and collect the futures
        List<CompletableFuture<?>> sends = new ArrayList<>(rows.size());
        for (Row r : rows) {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(r.topic(), r.key(), r.payload());   // topic comes from the row
            record.headers().add("event-id", r.id().toString().getBytes(StandardCharsets.UTF_8));
            record.headers().add("event-type", r.eventType().getBytes(StandardCharsets.UTF_8));
            sends.add(kafka.send(record));
        }

        // 3. Wait until Kafka has acknowledged every message.
        //    If any send fails or times out this throws, the transaction rolls back,
        //    and the rows stay PENDING and are retried on the next run.
        CompletableFuture.allOf(sends.toArray(new CompletableFuture[0]))
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 4. Only reached when everything was acknowledged: mark the rows as published
        jdbc.batchUpdate(
                "update outbox_event set status = 'PUBLISHED', published_at = now() where id = ?",
                rows, 100,
                (ps, r) -> ps.setObject(1, r.id()));

        log.debug("Published {} outbox events", rows.size());
    }
}