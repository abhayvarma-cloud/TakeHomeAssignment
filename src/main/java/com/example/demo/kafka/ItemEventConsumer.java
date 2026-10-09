package com.example.demo.kafka;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.network.Send;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.AcknowledgeMode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.RetryTopicHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import com.example.demo.entity.NotificationTracker;
import com.example.demo.repository.NotificationTrackerRepository;

import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import jakarta.persistence.criteria.CriteriaBuilder.Case;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.kafka.common.header.Headers;      // only if you declare a Headers variable

import java.nio.charset.StandardCharsets;

@Service
public class ItemEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ItemEventConsumer.class);
    @Autowired
    private  NotificationTrackerRepository trackerRepo;
   
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
    
    
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            autoCreateTopics = "true",
            listenerContainerFactory = "kafkaListenerContainerFactory"
           )
    @KafkaListener(topics = "notification-events", groupId = "demo-group",  containerFactory = "kafkaListenerContainerFactory")
    public void notify(ConsumerRecord<String, String> record,Acknowledgment ack,
                       @Header(name = "event-id", required = false) String eventId,
                       @Header(name = "event-type", required = false) String eventType,
                       @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false) Integer attemptHeader) {

        int attempt = (attemptHeader == null) ? 1 : attemptHeader;     // no header on the first delivery
        String objectId = record.key();

        // 1. Check the tracker, insert a PENDING row if none exists
        NotificationTracker tracker = trackerRepo.findByObjectIdAndType(objectId, eventType).orElse(null);
        if (tracker == null) {
            try {
                tracker = trackerRepo.save(new NotificationTracker(objectId, eventType));
            } catch (DataIntegrityViolationException e) {
                // another consumer inserted it first, so read that row
                tracker = trackerRepo.findByObjectIdAndType(objectId, eventType).orElseThrow();
            }
        }

        // 2. Status 1 (SENT): do nothing
        if (tracker.getStatus() == NotificationTracker.SENT) {
          
            return;
        }

        // 3. Status 0 (PENDING): send the email. A throw here means no ack,
        //    and the message moves to the next retry topic (then the DLT).
        sendEmail(record, attempt);

        // 4. Mark as sent
        tracker.markSent();
        trackerRepo.save(tracker);

        ack.acknowledge();     
        log.info("Consumed: key={} partition={} offset={} attempt={} value={}",
                 record.key(), record.partition(), record.offset(), attempt, record.value());
    }

    private void sendEmail(ConsumerRecord<String, String> record, int attempt) {
        if (attempt <= 1) {
            throw new IllegalStateException("Simulated failure on attempt " + attempt);   // test stub
        }
        // call your email provider here
    }
  
}
