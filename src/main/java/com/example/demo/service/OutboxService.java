package com.example.demo.service;

import com.example.demo.entity.OutboxEvent;
import com.example.demo.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private final OutboxRepository outboxRepo;
    private final ObjectMapper mapper;

    public OutboxService(OutboxRepository outboxRepo, ObjectMapper mapper) {
        this.outboxRepo = outboxRepo;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void add(String topic, String aggregateType, String aggregateId,
                    String eventType, Object payload) {
        try {
            outboxRepo.save(new OutboxEvent(topic, aggregateType, aggregateId, eventType,
                                            mapper.writeValueAsString(payload)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox payload", e);
        }	
    }
}