package com.example.demo.entity;

import java.time.Instant;
import java.util.UUID;


import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "aggregate_type", nullable = false) private String aggregateType;
    @Column(name = "aggregate_id", nullable = false)   private String aggregateId;
    @Column(name = "event_type", nullable = false)     private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;
    
    
    @Column(nullable = false, length = 249)
    private String topic;

    protected OutboxEvent() {}

   

    public OutboxEvent(String topic, String aggregateType, String aggregateId,
                       String eventType, String payload) {
        this.topic = topic;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

   
}
