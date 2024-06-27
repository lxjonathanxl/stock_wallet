package com.shares.wallet.Proxy.notification.kafka;

import com.shares.wallet.model.History;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final static Logger producerLogger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, History> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, History> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topicName, History value) {
        var future = kafkaTemplate.send(topicName, value);
        future.whenComplete((sendResult, exception) -> {
            if (exception != null) {
                future.completeExceptionally(exception);
            } else {
                future.complete(sendResult);
            }
            producerLogger.info(String.format("Task status send to Kafka topic : %s, Object: ", topicName)+ value);
        });
    }
}