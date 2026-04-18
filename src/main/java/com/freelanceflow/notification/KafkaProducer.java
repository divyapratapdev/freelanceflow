package com.freelanceflow.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    
    private final java.util.Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    public KafkaProducer(java.util.Optional<KafkaTemplate<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, Object payload) {
        if (kafkaTemplate.isPresent()) {
            log.info("Sending message to topic={}, key={}, payload={}", topic, key, payload.getClass().getSimpleName());
            kafkaTemplate.get().send(topic, key, payload);
        } else {
            log.info("Kafka is disabled (Render Profile). Simulated sending msg to topic={}, key={}", topic, key);
        }
    }
}
